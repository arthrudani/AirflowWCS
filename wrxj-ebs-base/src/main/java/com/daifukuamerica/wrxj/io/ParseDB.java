package com.daifukuamerica.wrxj.io;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *   Driver program that uses the ParseDatabaseDef and TranslationDef objects.
 *   In order to use this class the user must have three things in place:
 *
 *       (1) The environment variable DDLPATH must be defined to point to
 *           where the Data Definition (DDL) file "jwmsadd.sql" is located.
 *
 *        (2) A sub-directory called "TranTemplate" must be present under the
 *            same directory as where this program is run.  This directory
 *            must have the file "DBTrans.Template" in it.
 *
 *        (3) Use the -D flag in the run time engine and define the var.
 *            DDLPATH on the command line. For example:
 *            java -D "DDLPATH"=%DDLPATH% for the NT environment, where
 *            %DDLPATH% is a an environment var. pointing to the DDL file.
 *
 * @author 21-Aug-01    A.D.      Original version
 *
 *-End_Doc_Sub
 */
public class ParseDB
{
  public static void main(String[] args)
  {
    String output_dir = System.getProperty("DDLPARSEOUT");
    String ddlFile = System.getProperty("DDLFILE");
    String templateFilePath = System.getProperty("TRAN_TEMPLATE_PATH");

                                       // Read info. off command line -D flag.
    if (output_dir == null || output_dir.length() == 0)
    {
      System.err.println("DDLPARSEOUT is set to null! Output directory not found...");
      System.exit(-1);
    }

    if (ddlFile == null || ddlFile.length() == 0)
    {
      System.err.println("DDLFILE is set to null! DDL file not found...");
      System.exit(-1);
    }

    ParseDatabaseDef db = new ParseDatabaseDef(output_dir, ddlFile, false);
    TranslationDef trn = new TranslationDef(output_dir, ddlFile, templateFilePath,
                                            true);

    db.parseFile();                    // Generate DBConstants.java file that
                                       // contains char field length constants

/*---------------------------------------------------------------------------
 Add translation constants to file DBConstants.java and generate temporary
 file TmpTrans.java containing Translation object arrays.
---------------------------------------------------------------------------*/
    trn.parseFile();

    try
    {                                  // Merge TmpTrans.java with template
                                       // file DBTrans.Template to create
                                       // DBTrans.java in current directory.
      trn.updateDBTranClass();
    }
    catch(FileNotFoundException e)
    {
      System.err.println(e);
    }
    catch(IOException e)
    {
      e.printStackTrace(System.out);
    }
  }
}
