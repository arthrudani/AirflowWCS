package com.daifukuamerica.wrxj.web.core.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



/**
 * Maps classes to classes. OVERRIDDEN FROM BASE FOR USE WITH WEB APPS CLASSPATH
 * 
 * <p><b>Details:</b> This class maps classes to classes, and is provided 
 * primarily to support the {@link Factory} class.  Use this class to insert
 * associations into the factory.</p>
 * 
 * @see Factory
 * @author Sharky
 */
public final class ImplementationMapping extends HashMap<Class, Class>
{
  private static final long serialVersionUID = 0;

  /**
   * <p><b>Details:</b> This method adds to the current mapping the associations
   * parsed from the given URL.  If conflicts occur, the association read from 
   * the URL will overwrite the associations already stored.</p>
   * 
   * <p>If an I/O error occurs while reading from the URL, this method throws an 
   * {@link IOException}.  If an association references a non-<wbr>existent 
   * class, this method throws a {@link ClassNotFoundException}.</p>
   * 
   * @param isProperties
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if an unresolveable class is referenced
   */
  private void loadFromProperties() 
    throws IOException, ClassNotFoundException
  {
		Properties properties = new Properties(); 
		try 
		{
			properties.load(ImplementationMapping.class.getClassLoader().getResourceAsStream("factory.properties"));
		} 
		catch (IOException e) 
		{

		}
	      for(Map.Entry vpEntry: properties.entrySet())
	      {
	        String vsKey = (String)vpEntry.getKey();
	        String vsValue = (String) vpEntry.getValue();
	        Class vtInterface = Class.forName(vsKey.trim());
	        Class vtImplementation = Class.forName(vsValue.trim());
	        put(vtInterface, vtImplementation);
	      }	
  }
  
  /**
   * Creates new instance from properties file.
   * 
   * <p><b>Details:</b> This factory method populates a new instance from the
   * text file found at the given URL.  The format of the text file is that of a 
   * standard Java properties file, where the key is the fully qualified class
   * name of the interface type, and the value is the fully qualified class name
   * of the implementing type.</p>
   * 
   * <p>If the mapping found in the properties file includes a non-<wbr>existent
   * class (which might indicate a typo in the text file), a 
   * {@link ClassNotFoundException} will be thrown.</p>
   *  
   * @param isResource the path and name of the resource
   * @return the populated instance
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if an unresolveable class is referenced
   */
  public static ImplementationMapping createMapping() 
    throws IOException, ClassNotFoundException
  {
	ImplementationMapping vpImplementationMap = new ImplementationMapping();
    vpImplementationMap.loadFromProperties();
    return vpImplementationMap;
  }
  
}

