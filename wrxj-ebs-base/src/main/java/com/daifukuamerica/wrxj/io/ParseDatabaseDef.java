package com.daifukuamerica.wrxj.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Title: ParseDatabaseDef
 * Description: Class for parsing a DDL (Database definition language) <BR>
 *              file.  This class will also generate code for storing all<BR>
 *              translations into a hash table for the DBTrans class.
 * @author       A.D.
 * @version      1.0
 * <BR>Created: 15-Aug-01<BR>
 *     Copyright (c) 2001<BR>
 *     Company:  SKDC Corporation
 */
public class ParseDatabaseDef
{
  protected final String EOL_CHAR = System.getProperty("line.separator");


  protected String  sDDLFile = "";     // Full path and file name to DDL file
  protected String  sOutputDirectory = "";
  protected String  sPackageName;
  protected boolean putEndingBrace;
  protected int     maxTokenLength;
  protected Map     trMap = new TreeMap();
  protected   String  constantFile = "";
  private   String[] keySymbols = { "CONSTRAINT",
                                    "USING",
                                    "PRIMARY",
                                    "REM",
                                    "--",
                                    "USING",
                                    "TABLESPACE"
                                  };

  public ParseDatabaseDef()            // Default constructor
  {
    super();
    maxTokenLength = 0;
    putEndingBrace = false;
  }

  public ParseDatabaseDef(String isOutputDirectory, String isDDLFile,
                          boolean izPutEndingBrace)
  {
    this();
    putEndingBrace = izPutEndingBrace;
    sOutputDirectory = isOutputDirectory;
    sDDLFile = isDDLFile;

                                       // Name and path to file written by
                                       // this class.
    constantFile = sOutputDirectory + File.separator + "DBConstants.java";
    
    Pattern p = Pattern.compile("com?");
    Matcher m = p.matcher(isOutputDirectory);
    if (m.find())
    {
      String mySubstring = isOutputDirectory.substring(m.start());
      // This is for a batch file run
      mySubstring = mySubstring.replace('\\', '.');
      // This is for a ant run
      mySubstring = mySubstring.replace('/', '.');
      sPackageName = mySubstring;
    }
    else
    {
      System.out.println("Error parsing package name from output path.  Defaulting.");
      sPackageName = "com.daifukuamerica.wrxj.jdbc";
    }

    String sWarning = "";

    sWarning = "package " + sPackageName + ";" + EOL_CHAR + EOL_CHAR;
    sWarning += "/**" + EOL_CHAR;
    sWarning += " * Database constants and translation values." + EOL_CHAR;
    sWarning += " * <P>" + EOL_CHAR;
    sWarning += " * This file is automatically generated.  Please do not edit!" + EOL_CHAR;
    sWarning += " * To change this file, modify jwmsadd.sql and run \"ant db-tran\" to regenerate" + EOL_CHAR;
    sWarning += " * this file." + EOL_CHAR;
    sWarning += " * </P>" + EOL_CHAR;
    sWarning += " */" + EOL_CHAR;

    try
    {
      FileWriter FOut = new FileWriter(constantFile);

      FOut.write(sWarning);
      FOut.flush();
      FOut.close();
    }
    catch(IOException e)
    {
      e.printStackTrace(System.out);
      System.exit(-1);
    }
  }

  /**
   * Routine returns true if "str" is a given SQL keyword.
   */
  private boolean hasIgnorableKeyWord(String str)
  {
    for(int key = 0; key < keySymbols.length; key++)
    {
      if (keySymbols[key].equalsIgnoreCase(str))
      {
        return(true);
      }
    }

    return(false);
  }

  /**
   * Method does second parsing phase for hash table elements to derive length
   * definitions.
   */
  protected int writeConstants() throws IOException
  {
    String vsFormat = "%-" + (maxTokenLength+2) + "." + (maxTokenLength+2) + "s";

    try
    {
      FileWriter FOut = new FileWriter(constantFile, true);

      FOut.write("public interface DBConstants" + EOL_CHAR);
      FOut.write("{" + EOL_CHAR);

/*===========================================================================
            Write any custom, user-defined non-DDL file constants here.
  ===========================================================================*/
      FOut.write("  /*------------------------------------------*" + EOL_CHAR);
      FOut.write("   *          Non-DDL constants.              *" + EOL_CHAR);
      FOut.write("   *------------------------------------------*/" + EOL_CHAR);

                                       // Define Write-lock flag for DB.
      FOut.write("  final int ");
      FOut.write(String.format(vsFormat, "WRITELOCK"));
      FOut.write(" = 666;" + EOL_CHAR);

                                       // Define Write-lock flag for DB.
      FOut.write("  final int ");
      FOut.write(String.format(vsFormat, "NOWRITELOCK"));
      FOut.write(" = 667;" + EOL_CHAR);
                                       // Define Location BANK size.
      FOut.write("  final int ");
      FOut.write(String.format(vsFormat, "LNBANK"));
      FOut.write(" = 3;" + EOL_CHAR);
                                       // Define Location BAY size.
      FOut.write("  final int ");
      FOut.write(String.format(vsFormat, "LNBAY"));
      FOut.write(" = 3;" + EOL_CHAR);
                                       // Define Location TIER size.
      FOut.write("  final int ");
      FOut.write(String.format(vsFormat, "LNTIER"));
      FOut.write(" = 3;" + EOL_CHAR);
                                       // Define Location Height size.
      FOut.write("  final int ");
      FOut.write(String.format(vsFormat, "LNHEIGHT"));
      FOut.write(" = 2;" + EOL_CHAR);
                                       // Define Aisle-Group size.
      FOut.write("  final int ");
      FOut.write(String.format(vsFormat, "LNAISLEGROUP"));
      FOut.write(" = 2;" + EOL_CHAR);

/*===========================================================================
                      End user-defined section.
  ===========================================================================*/
      if (putEndingBrace)
      {
        FOut.write("}" + EOL_CHAR);
        System.out.println(EOL_CHAR + EOL_CHAR);
        System.out.println("Generated File \"DBConstants.java\"" + EOL_CHAR);
      }
      FOut.flush();
      FOut.close();
    }
    catch(IOException e)
    {
      throw e;
    }

    return(0);
  }

  /**
   * Method does first parsing phase using DB definition file and builds
   * Hash Table of necessary tokens.
   */
  protected void parseForConstants()
  {
    int        tokenLength;
    String     sLine;
    String[]   sTok;
    boolean found_create, done;

    found_create = done = false;

    try
    {
                                       // Set up char input stream from file
      FileReader FInp = new FileReader(sDDLFile);

                                       // Set up Line reader feeding off the
                                       // input stream.
      LineNumberReader rd = new LineNumberReader(FInp);

      while(!done)
      {
        if ((sLine = rd.readLine()) != null)
        {
                                       // Break up the string using spaces.
          if (sLine.startsWith("CREATE TABLE ") ||
              sLine.startsWith("create table "))
          {
            found_create = true;
            continue;
          }
        }
        else
        {
          done = true;
        }
                                       // Found the CREATE statement. So
                                       // start parsing the record definition
        if (!done && found_create)
        {
          if (hasIgnorableKeyWord(sLine))
          {
            continue;
          }
          else if (sLine.indexOf("VARCHAR2") >= 0 ||
                   sLine.indexOf("varchar2") >= 0 ||
                   sLine.indexOf("VARCHAR") >= 0  ||
                   sLine.indexOf("varchar") >= 0)
          {
                                       // Tokenize the current line
            Scanner sTokenRef = new Scanner(sLine);
            List<String> tokList = new ArrayList<String>();
            while (sTokenRef.hasNext())
              tokList.add(sTokenRef.next());
            sTok = tokList.toArray(new String[tokList.size()]);

                                       // Add string objects to the hash table.
                                       // The table will be a naturally sorted.
                                       // -- NOTE: skip the first character
                                       //    in the key definition
            if (!trMap.containsKey(sTok[0].substring(1)))
            {
              trMap.put(sTok[0].substring(1), sTok[1]);

                                       // Figure out the maximum column length
                                       // for the key and element of the hash
                                       // table for formatted output later.
              tokenLength = sTok[0].length();
              if (tokenLength > maxTokenLength)
              {
                maxTokenLength = tokenLength;
              }
/* For DEBUG
System.out.println("Token=" + sTok[0].substring(1) + " = " + sTok[1]);
*/
            }
          }
          else if (sLine.startsWith(")"))
          {
              found_create = false;
          }
        }
      }

      FInp.close();
                                       // Write the data to a file.
      writeConstants();
      trMap.clear();                   // Wipe out Map.
      trMap = null;
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

  public void parseFile()
  {                                    // Generate Interface file containing
    parseForConstants();               // constant field lengths.
  }
}
