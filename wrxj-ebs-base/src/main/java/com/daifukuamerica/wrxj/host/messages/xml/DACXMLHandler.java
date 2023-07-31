
package com.daifukuamerica.wrxj.host.messages.xml;

import com.daifukuamerica.wrxj.dbadapter.data.HostToWrxData;
import com.daifukuamerica.wrxj.host.InvalidHostDataException;
import com.daifukuamerica.wrxj.host.messages.HostError;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Helper class that implements common methods across all XML parsers.  This class
 * takes advantage of the DefaultHandler2 interface which allows us to easily
 * use a LexicalHandler to catch DTD parser errors before reaching the document 
 * parser phase.
 * 
 * @author   A.D.
 * @version  1.0 
 * @since    20-Oct-2008 
 */
public class DACXMLHandler extends DefaultHandler2
{
  protected final String    DUMMY_DTD_FILE = "wrxj.dtd";
  protected HostToWrxData   mpHIData;
  protected InvalidHostDataException mpIHE;
  protected DBObject        mpDBObj;
  protected StringBuffer    mpPCData;
  protected String          msRootElement;
  protected Logger          mpLogger = Logger.getLogger();
  private int               mnCurrentAction;
  
 /**
  * Default constructor.
  * @throws DBException if there is a database connection error.
  */
  public DACXMLHandler() throws DBException
  {
    mpDBObj = new DBObjectTL().getDBObject();
    mpPCData = new StringBuffer();
  }

 /**
  * Method sets up and calls a SAX parser.
  * @param ipHIData Data from host.
  * @throws InvalidHostDataException when there are parsing problems.
  */
  public void parse(HostToWrxData ipHIData) throws InvalidHostDataException
  {
    byte[] vabMessageBytes = ipHIData.getMessageBytes();
    mpHIData = ipHIData;
    
    InputStream vpInputStream = null;  
    String vsErrorInfoMessageSamp;

    if (vabMessageBytes.length > 150)
      vsErrorInfoMessageSamp = new String(vabMessageBytes, 0, 150);
    else
      vsErrorInfoMessageSamp = new String(vabMessageBytes, 0, vabMessageBytes.length);

    vsErrorInfoMessageSamp = "<![CDATA[" + vsErrorInfoMessageSamp + "]]>";


    try
    {
      XMLReader vpXMLReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
                                       // Register the Content Handler (which is
                                       // this object itself)
      vpXMLReader.setContentHandler(this);
      vpXMLReader.setErrorHandler(this);
      vpXMLReader.setEntityResolver(this);
                                       // Make it a validating parser.
      vpXMLReader.setFeature("http://xml.org/sax/features/validation", true);
      vpXMLReader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
/*---------------------------------------------------------------------------
   Wrap our data into an InputSource and call the SAX parser. The parser then
   does callbacks to its registered Content Handler.
  ---------------------------------------------------------------------------*/
      vpInputStream = new ByteArrayInputStream(vabMessageBytes); 
      vpXMLReader.parse(new InputSource(vpInputStream));
    }
    catch(SAXException exc)
    {
      Exception vpExc = exc.getException();
      if (vpExc != null && vpExc instanceof InvalidHostDataException)
        throw (InvalidHostDataException)vpExc;
      else
        throw new DBRuntimeException(exc);
    }
    catch(IOException exc)
    {
      throw new InvalidHostDataException("Data parsing error for " +
                                         vsErrorInfoMessageSamp, exc);
    }
    catch(Exception exc)
    {
      throw new InvalidHostDataException(vsErrorInfoMessageSamp, exc);
    }
    catch(Throwable the)
    {
      throw new InvalidHostDataException(vsErrorInfoMessageSamp, the);
    }
    finally
    {
      if (vpInputStream != null)
      {
        try { vpInputStream.close(); } catch(IOException ioe) {}
      }
    }
  }

 /**
  * Method to determine validity of DOCTYPE declaration. This method is called
  * by the parser during a preliminary phase of parsing (before hitting the 
  * startDocument method).  The following checks are made:
  * <ul>
  *   <li>The root element must be specified!  This is validated against the 
  *       HostToWrx record's message identifier field.</li>
  *   <li>The System Identifier field must be provided and contain the dummy 
  *       file reference wrxj.dtd.</li>
  * </ul>
  * @param isRootElementName the root element of the message.
  * @param isPublicID the PUBLIC ID if it's used.
  * @param isSystemID the SYSTEM ID specifying the DTD file to use. This should
  *        always be set to wrxj.dtd at this stage of parsing.
  * @throws SAXException any exception the user may choose to raise based on
  *         reported parameters.  In this implementation we check the validity
  *         of the SYSTEM attribute.
  */
  @Override
  public void startDTD(String isRootElementName, String isPublicID, 
                       String isSystemID) throws SAXException
  {
    String vsSystemID = (isSystemID != null) ? isSystemID.trim() : "";
    String vsRootElement = (isRootElementName != null) ? isRootElementName.trim() : "";
    String vsMesgIdentifier = mpHIData.getMessageIdentifier();
    String vsErr = "";
    if (vsRootElement.isEmpty() || !vsRootElement.equals(vsMesgIdentifier))
    {
      vsErr = "DOCTYPE declaration does not have the required message root " +
              "name correctly specified! It should be \"" + vsMesgIdentifier + 
              "\".  Message parsing stopped.";
    }
    else if (vsSystemID.isEmpty() || !vsSystemID.equals(DUMMY_DTD_FILE))
    {
      vsErr = "DOCTYPE declaration does not have the required SYSTEM identifier " +
              "set correctly for the root element " + isRootElementName + 
              "! Please refer to the host specification. Message parsing stopped.";
    }
    
    if (!vsErr.isEmpty())
    {
      mpIHE = new InvalidHostDataException(vsErr);
      mpIHE.setOriginalSequence(mpHIData.getOriginalMessageSequence());
      throw new SAXException(mpIHE);
    }
    
    msRootElement = isRootElementName;
  }

 /**
  * Method accumulates characters as they are returned by the parser.
  * 
  * @param iacBuf accumulation buffer where groups of characters are returned by
  *        the parser.  <b>Note:</b> This buffer is not guaranteed to have all data
  *        between a #PCDATA element in one call by the parser.  As a result
  *        data is accumulated until the parser tells us that it is done
  *        processing the data within the tag (it is done when the endElement
  *        method is called).  This may seem abnormal but it is quite normal for
  *        large data streams, and multi-byte characters.
  * @param inStartPos the start position of newest returned chunk within 
  *        accumulation buffer iacBuf.
  * @param inLength the number of bytes returned.
  * @throws SAXException for parsing problems.
  */
  @Override
  public void characters(char[] iacBuf, int inStartPos, int inLength) throws SAXException
  {
    mpPCData.append(iacBuf, inStartPos, inLength);
  }
  
  @Override
  public void error(SAXParseException sax_exc) throws SAXException
  {
    System.out.print("Error Validating: " + sax_exc.getLineNumber());
    System.out.println(",  Column: " + sax_exc.getColumnNumber());
    System.out.println("\nMessage: " + sax_exc.getMessage());
    InvalidHostDataException invDataExc = new InvalidHostDataException(sax_exc);
    invDataExc.setOriginalSequence(mpHIData.getOriginalMessageSequence());
    invDataExc.setErrorCode(HostError.INVALID_DATA);
    invDataExc.setErrorMessage(sax_exc.getMessage() +
                               ". Format validation error ItemMessage. ");
    throw new SAXException(invDataExc);
  }
  
  @Override
  public void fatalError(SAXParseException sax_exc) throws SAXException
  {
    System.out.print("Error at Line: " + sax_exc.getLineNumber());
    System.out.println(",  Column: " + sax_exc.getColumnNumber());
    System.out.println("\nMessage: " + sax_exc.getMessage());
    InvalidHostDataException invDataExc = new InvalidHostDataException(sax_exc);
    invDataExc.setOriginalSequence(mpHIData.getOriginalMessageSequence());
    invDataExc.setErrorCode(HostError.INVALID_DATA);
    invDataExc.setErrorMessage(sax_exc.getMessage() +
                               ". Format validation error ItemMessage. ");
    throw new SAXException(invDataExc);
  }

  public void setCurrentAction(String isAction)
  {
    mnCurrentAction = (isAction.equals("ADD"))    ? DBConstants.ADD :
                      (isAction.equals("MODIFY")) ? DBConstants.MODIFY
                                                  : DBConstants.DELETE;
  }
  
  /**
   * Added this base method so we can still get base to work with the new xerces jars
   * {@inheritDoc} This method allows for dynamically changing which DTD
   * handler the parser will use.  This is particularly useful when we don't want
   * the producer of this XML message to know anything about what type of validations
   * occur on this side.
   * @param isPublicID {@inheritDoc} This parameter is not used in this implementation
   *        since it is not specified in the default DOCTYPE entity in use here.
   * @param isSystemID {@inheritDoc} This identifier will always be set to the
   *        default of "wrxj.dtd" when the message is retrieved from the inbound
   *        data queue (HostToWrx table for most implementations).  This makes
   *        this object (which is the only one that really cares) responsible for
   *        pointing to the real and correct DTD to use for parsing.
   * @return {@inheritDoc}
   */
   @Override
   public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
          throws SAXException, IOException
   {
     return resolveEntity(publicId, systemId);
   }
  
  public int getCurrentAction()
  {
    return(mnCurrentAction);
  }
}
