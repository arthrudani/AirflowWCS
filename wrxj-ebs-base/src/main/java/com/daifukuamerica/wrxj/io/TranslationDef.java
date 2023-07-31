package com.daifukuamerica.wrxj.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

/**
 * Title: TranslationDef
 * Description: Class for defining Translation constants.  This class will <BR>
 *              also generate code for storing all translations into a hash<BR>
 *              table for the DBTrans class.
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 27-Aug-01<BR>
 *     Copyright (c) 2001<BR>
 *     Company:  SKDC Corporation
 */
public class TranslationDef extends ParseDatabaseDef
{
  private boolean gen_newfile = true;
  private String  templatePath;
  private static String TEMP_FILENAME = "TmpTran.java";

  public TranslationDef()              // Default Constructor.
  {
    super();
  }

  public TranslationDef(String isOutputDirectory, String isDDLFile,
                        String isTemplatePath, boolean izPutEndingBrace)
  {
    this();
    putEndingBrace = izPutEndingBrace;
    sOutputDirectory = isOutputDirectory;
    sDDLFile = isDDLFile;
    templatePath = isTemplatePath;
  }

  /**
   * Method outputs the contents of the translation hash table into name-value
   * pairs into DBConstants.java.
   */
  @Override
  protected int writeConstants() throws IOException
  {
    String sSymbolicName = "";
    String sNumericDef = "";
    FileWriter FOut = null;

    try
    {                                  // Open the file in append mode.
      constantFile = sOutputDirectory + File.separator + "DBConstants.java";
      FOut = new FileWriter(constantFile, true);

      FOut.write(EOL_CHAR + EOL_CHAR);
      FOut.write("  /*------------------------------------------*" + EOL_CHAR);
      FOut.write("   *          Translation constants.          *" + EOL_CHAR);
      FOut.write("   *------------------------------------------*/" + EOL_CHAR);

                                       // Get a set containing all hash table
                                       // key entries.
      Set hashKeys = trMap.keySet();

                                       // Build iterator to iterate over set
                                       // of entries.
      Object   keyValue;
      Iterator itr = hashKeys.iterator();

      while(itr.hasNext())
      {
        keyValue = itr.next();         // Retrieve next table element.

        sSymbolicName = keyValue.toString();
        sNumericDef = trMap.get(keyValue).toString();

        FOut.write("  final int ");
        FOut.write(String.format("%-" + maxTokenLength + "." + maxTokenLength + "s",
                                 sSymbolicName));
        FOut.write(" = " + sNumericDef + ";" + EOL_CHAR);
      }

      if (putEndingBrace)
      {
        FOut.write("}" + EOL_CHAR);
        System.out.println(EOL_CHAR + EOL_CHAR + "Generated File \"DBConstants.java\"" + EOL_CHAR);
      }
    }

    catch(IOException e)
    {
      throw e;
    }
    finally
    {
      if (FOut != null)
      {
        FOut.flush();
        FOut.close();
      }
    }

    return(0);
  }

  /**
   * Method writes the DBTrans constructor into a temporary file. In particular
   * this routine writes out the DBTrandef object arrays in the DBTrans
   * constructor.
   */
  private void writeTranslationClass(String trans_name, Map tr_map)
          throws IOException
  {
    int    idx;
    String sOutputFile = "";
    String sSymbolicName = "";
    String sTranString   = "";
    RandomAccessFile rafile = null;

    try
    {
      if (gen_newfile)
      {                                // Create a new file in the TranTemplate
                                       // dir.  Delete old file if it exists.
        sOutputFile = templatePath + File.separator + TranslationDef.TEMP_FILENAME;
        File fp = new File(sOutputFile);
        if (fp.exists())
        {
          fp.delete();
        }

        rafile = new RandomAccessFile(fp, "rw");
        rafile.writeBytes("  public static void init()" + EOL_CHAR + "  {" + EOL_CHAR);
        rafile.writeBytes("    if (tm.size() == 0)" + EOL_CHAR + "    {" + EOL_CHAR);
        gen_newfile = false;
      }
      else
      {
        sOutputFile = templatePath + File.separator + TranslationDef.TEMP_FILENAME;
        rafile = new RandomAccessFile(sOutputFile, "rw");

                                // Reposition to where '}' is.
        for(long fp_pos = rafile.length() - 1; fp_pos > 0; fp_pos--)
        {
          rafile.seek(fp_pos);
          if (rafile.readByte() == '}')
          {
            rafile.seek(fp_pos-5);
            break;
          }
        }
      }
                                  // Build iterator to iterate over set
                                  // of entries.
      Object   keyValue;
      Iterator itr = tr_map.keySet().iterator();

                                  // Write various output to file.
      sOutputFile = EOL_CHAR + EOL_CHAR + "                                      // Object array";
      sOutputFile += " for \"" + trans_name + "\"" + EOL_CHAR;
      rafile.writeBytes(sOutputFile);
                                  // iLocationStatus will always have one
                                  // less entry than what's in the DB
                                  // due to the "Prohibit" setting.
      sOutputFile = "      DBTrandef[] " + trans_name + " = new DBTrandef[";
      sOutputFile += Integer.toString(tr_map.size()) + "];" + EOL_CHAR;
      rafile.writeBytes(sOutputFile);
                                       // Start iterating over Set.
      for(idx = 0; itr.hasNext(); idx++)
      {
        keyValue = itr.next();         // Retrieve next table element.

        sSymbolicName = keyValue.toString();
        sTranString = tr_map.get(keyValue).toString();

        sOutputFile = "      " + trans_name + "[" + idx + "] = ";
        sOutputFile += "new DBTrandef(" + sSymbolicName + ", ";
        sOutputFile += sTranString + ");" + EOL_CHAR;
        rafile.writeBytes(sOutputFile);
      }

      sOutputFile = EOL_CHAR;
      sOutputFile += ("      tm.put(\"" + trans_name.toUpperCase() + "\", ");
      sOutputFile += (trans_name + ");" + EOL_CHAR);
      rafile.writeBytes(sOutputFile);

/*---------------------------------------------------------------------------
  Write closing brace.  Note: if there happens to be more object arrays to set
  up, then this closing brace is over-written. Once the new object array is
  written, the brace is re-inserted (this is why the RandomAccessFile object
  is used here; it allows more control over file pointer positioning).
  ---------------------------------------------------------------------------*/
      rafile.writeBytes("    }" + EOL_CHAR);
      rafile.writeBytes("  }" + EOL_CHAR);
    }

    catch(IOException e)
    {
      throw e;
    }
    finally
    {
      if (rafile != null)
      {
        rafile.close();
      }
    }
  }

  /**
   * Routine parses and retrieves translations from the jwmsadd.sql file.
   */
  @Override
  protected void parseForConstants()
  {
    boolean  found_trans = false;
    int      iTmp, tokenLength;
    String   tranInt      = "";
    String   sReadLine    = "";
    String   tranSymbName = "";
    String   tranStr      = "";
    String   tranName     = "";
    String[] sTok;
    Map  trMapClass = new TreeMap();

    try
    {
      FileReader fr = new FileReader(sDDLFile);
                                       // Set up Line reader feeding off the
                                       // input stream.
      LineNumberReader rd = new LineNumberReader(fr);

      while((sReadLine = rd.readLine()) != null)
      {
        if (sReadLine.indexOf("TRANBEGIN") > 0)
        {
          Scanner sTokenRef = new Scanner(sReadLine);
          List<String> tokList = new ArrayList<String>();
          while (sTokenRef.hasNext())
            tokList.add(sTokenRef.next());
          sTok = tokList.toArray(new String[tokList.size()]);
          tranName = sTok[2];

          if (!trMap.containsKey(tranName))
          {
            found_trans = true;
            trMapClass.clear();
            continue;
          }
        }
        else if (sReadLine.indexOf("TRANEND") > 0)
        {
          found_trans = false;
          writeTranslationClass(tranName, trMapClass);
        }

        if (found_trans)
        {
          sTok = sReadLine.split(",");
          if (sTok == null)
          {
            continue;
          }
                              // Skip the "Rem" and get const. name.
          if ((iTmp = sTok[0].indexOf("Rem")) > 0 ||
               sTok[0].startsWith("Rem"))
          {
            iTmp += 3;
          }

          tranSymbName = sTok[0].substring(iTmp).trim();

          tranInt = sTok[1].trim();

                              // Ignore any field comments
          int comment_idx = 0;
          if ((comment_idx = sTok[2].indexOf("--")) > 0)
          {
            tranStr = sTok[2].substring(0, comment_idx).trim();
          }
          else
          {
            tranStr = sTok[2].trim();
          }

          if (!trMap.containsKey(tranName))
          {
                                       // Map for DBConstants.java file.
            trMap.put(tranSymbName, tranInt);
                                       // Map for DBTrans class file.
            trMapClass.put(tranSymbName, tranStr);

                                       // Figure out the maximum column length
                                       // for the key and element of the hash
                                       // table for formatted output later.
            tokenLength = tranSymbName.length();
            if (tokenLength > maxTokenLength)
            {
              maxTokenLength = tokenLength + 1;
            }
          }
        }
      }

      fr.close();

      writeConstants();
    }
    catch(UnsupportedOperationException e)
    {
      System.err.println(e);
    }
    catch(NoSuchElementException e)
    {
      System.err.println(e);
    }
    catch(FileNotFoundException e)
    {
      System.err.println(e);
    }
    catch(SecurityException e)
    {
      System.err.println(e);
    }
    catch(IOException e)
    {
      e.printStackTrace(System.out);
    }
  }

  /**
   * Routine updates the template DBTrans file with translation object
   * definitions.
   */
  public void updateDBTranClass()
         throws FileNotFoundException, IOException
  {
    boolean found_constructor = false;
    File    fpTmp = null;
    String  sLine    = "";
    String  sLineTmp = "";
    String  sTemplateFile = "";
    String  sDBTransFile = "";

    try
    {
                                       // Create path to DBTrans.Template file.
      sTemplateFile = templatePath + File.separator + "DBTrans.Template";
      FileReader fRead = new FileReader(sTemplateFile);

      fpTmp = new File(templatePath + File.separator + TranslationDef.TEMP_FILENAME);
      FileReader fTmp = new FileReader(fpTmp);

      sDBTransFile = sOutputDirectory + File.separator + "DBTrans.java";
      FileWriter fWrite = new FileWriter(sDBTransFile);

                                       // Set up Line readers feeding off the
                                       // input streams.
      LineNumberReader lineRead  = new LineNumberReader(fRead);
      LineNumberReader lineTmp = new LineNumberReader(fTmp);

/*---------------------------------------------------------------------------
   The following block does: fRead --> fWrite until pattern "public DBTrans"
   arises; then fTmp --> fWrite; ignore fRead stream until pattern "}"
   arises; then fRead --> fWrite again.  Bascically it combines the contents
   of DBTrans.java and TranslationDef.TEMP_FILENAME to form the new DBTrans.java
   file.
  ---------------------------------------------------------------------------*/
      while((sLine = lineRead.readLine()) != null)
      {
        if (sLine.startsWith("  public static void init()"))
        {
          found_constructor = true;
          while((sLineTmp = lineTmp.readLine()) != null)
          {
            fWrite.write(sLineTmp + EOL_CHAR);
          }
          fTmp.close();
          fpTmp.delete();
        }
        else if (found_constructor && sLine.indexOf("}") >0)
        {
          found_constructor = false;
          continue;
        }

        if (!found_constructor)
        {
          fWrite.write(sLine + EOL_CHAR);
        }
      }

      fRead.close();                   // This is the DBTrans.Template file.

      fWrite.flush();                  // This is the DBTrans.java file.
      fWrite.close();
      
      System.out.println("Generated File \"DBTrans.java\"" + EOL_CHAR);
    }

    catch(FileNotFoundException e)
    {
      throw e;
    }
    catch(IOException e)
    {
      throw e;
    }
  }

  @Override
  public void parseFile()
  {
    parseForConstants();
  }
}
