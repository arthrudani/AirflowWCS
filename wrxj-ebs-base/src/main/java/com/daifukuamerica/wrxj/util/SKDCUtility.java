package com.daifukuamerica.wrxj.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SKDCUtility Class to contain miscellaneous utility methods.
 *
 * @author       A.D.
 * @author       sharky  added reflection methods for getting class names, and
 *                       methods for creating objects from parsed class names.
 * @since 23-Aug-01
 */
public class SKDCUtility
{
  private static Pattern mpIntPattern = Pattern.compile("\\p{Digit}+");
  private static DecimalFormat mpDecimalFmt = new DecimalFormat("#.##");
  private static DecimalFormat mpIntegerFmt = new DecimalFormat("#");

 /**
  *  Method pre-fills a passed in string with zeroes and returns it.
  *
  *  @param str <code>String</code> to be zero-filled.
  *  @param zeroPadLength <code>int</code> containing maximum number of
  *         zeroes to use.
  *
  *  @return <code>String</code> containing zero filled string.
  */
  public static String preZeroFill(String str, int zeroPadLength)
                throws IllegalArgumentException
  {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumIntegerDigits(zeroPadLength);
    nf.setGroupingUsed(false);

    return(nf.format(Integer.parseInt(str)));
  }

  /**
   * Converts a double value to the format isPattern and returns a String
   * @param isPattern
   * @param idValue
   * @return
   */
  public static String formatDouble(String isPattern,  Double idValue)
  {
    DecimalFormat vpDF =  new DecimalFormat(isPattern);
    String  vsOutput = vpDF.format(idValue);
    return vsOutput;
  }

 /**
  *  Converts a passed in number into a zero prefixed string.
  *
  *  @param number <code>int</code> to be converted to zero-filled string.
  *  @param zeroPadLength <code>int</code> containing maximum number of
  *         zeroes to use.
  *
  *  @return <code>String</code> containing zero filled string.
  */
  public static String preZeroFill(int number, int zeroPadLength)
                throws IllegalArgumentException
  {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumIntegerDigits(zeroPadLength);
    nf.setGroupingUsed(false);

    return(nf.format(number));
  }

  /**
   * Routine takes an input string and appends blanks to it to make it
   * inPaddingLength length.
   *
   * @param isInputString the string to blank pad.
   * @param inPaddingLength the length to blank pad to.
   * @return a new string with trailing blanks.
   */
  public static String spaceFillTrailing(String isInputString, int inPaddingLength)
  {
    String vsSpaced = String.format("%-" + inPaddingLength + "." + inPaddingLength + "s",
                                    isInputString);
    return(vsSpaced);
  }

  /**
   * Method adds spaces to the beginning of a string to make it in PaddingLength.
   * @param isInputString the string to pad
   * @param inPaddingLength the desired length of the string
   * @return a new string with leading blanks
   */
  public static String spaceFillLeading(String isInputString, int inPaddingLength)
  {
    String vsSpaced = String.format("%" + inPaddingLength + "s", isInputString);
    return(vsSpaced);
  }

 /**
  * Method returns a truncated double value so that all double values in the
  * system can be handled in a consistent way.
  *
  * @param idDoubleValue the value to trucate and round.
  * @return truncated value.
  */
  public static synchronized double getTruncatedDouble(double idDoubleValue)
  {
    return(Double.parseDouble(mpDecimalFmt.format(idDoubleValue)));
  }

 /**
  * Convenience method for returning Double object.
  *
  * @param idDoubleValue the value to trucate and round.
  * @return truncated value.
  */
  public static synchronized Double getTrucatedDoubleObj(double idDoubleValue)
  {
    return(Double.valueOf(mpDecimalFmt.format(idDoubleValue)));
  }

   /**
  * Method converts a double value into an int value.  This method will round
  * upwards correctly.
  * @param idDoubleValue  the value to truncate and round.
  * @return integer value.
  */
  public static synchronized int convertDoubleToInt(double idDoubleValue)
  {
    return(Integer.parseInt(mpIntegerFmt.format(idDoubleValue)));
  }

  /**
   * Method parses a string, and returns the characters after the last occurance
   * of the search string.  If the search string is not found, it returns the entire
   * String
   * @param isStringtoParse <code>String</code> the string to be parsed
   * @param isSearchString <code>String</code> the search character or String
   * @return String
   */
  public static String parseLastIndex(String isStringtoParse, String isSearchString)
  {
    int vndelimiter = isStringtoParse.lastIndexOf(isSearchString);
    if(vndelimiter == -1)
    {
      return isStringtoParse;
    }
    else
    {
      return isStringtoParse.substring(vndelimiter + 1);
    }

  }

  /**
   * Method checks if a String represents an integer.
   * @param isString String to test.
   * @return
   */
  public static boolean isIntegerValue(String isString)
  {
    Matcher vpMatcher = mpIntPattern.matcher(isString);
    return vpMatcher.matches();
  }

  /**
   * Routine tokenizes a string based on 'delim' character and returns an
   * array of string tokens.
   *
   * @param str <code>String</code> containing string to tokenize.
   * @param delim <code>String[]</code> containing delimiter to tokenize against.
   * @return ?
   */
  public static String[] getTokens(String str, String delim)
  {
    StringTokenizer sTokenRef = (delim.trim().length() == 0) ? new StringTokenizer(str)
                                             : new StringTokenizer(str, delim);
    List<String> tokList = new ArrayList<String>();
    while (sTokenRef.hasMoreTokens())
      tokList.add(sTokenRef.nextToken());
    return (tokList.toArray(new String[tokList.size()]));
  }

 /**
  *  Method takes a <code>List</code> of <code>Map</code> data and converts a
  *  column from this data list into a <code>String</code> array.
  *
  *  @param ipDataList <code>List</code> containing List of data to process.
  *  @param isColumnName <code>String</code> containing name of specific column
  *         from the <code>List</code> to turn into a <code>String</code>
  *         array.  It is assumed that the key names (column Names) within the
  *         <code>Map</code> are all consistently the same case.
  *  @return <code>String[]</code> containing column data from the ipDataList.
  */
  public static String[] toStringArray(List<Map> ipDataList,
                                       String isColumnName)
  {
    String isMapKeyName = isColumnName;
                                       // Return empty array.
    if (ipDataList.isEmpty()) return(new String[0]);
                                       // First check the type-case of the key
    Map<String,String> vpMap = ipDataList.get(0); // we should be searching by.
    if (!vpMap.containsKey(isMapKeyName))
    {
      isMapKeyName = isColumnName.toUpperCase();
      if (!vpMap.containsKey(isMapKeyName))
      {
        isMapKeyName = isColumnName.toLowerCase();
        if (!vpMap.containsKey(isMapKeyName))
        {
          return(new String[0]);       // Return empty array.
        }
      }
    }

    String[] vpStringArray = new String[ipDataList.size()];
    for(int idx = 0; idx < ipDataList.size(); idx++)
    {
      vpStringArray[idx] = (ipDataList.get(idx)).get(isMapKeyName).toString();
    }

    return(vpStringArray);
  }

 /**
  * Method to load contents of a newline delimited file into a string array.  Any
  * line beginning with a # sign or blank lines are ignored.
  * @param isFileName the name of the file.  <b>This must include the full path
  *        specification also.</b>
  *
  * @return List of the lines in the file. Empty array if no lines are
  *         read from a file.
  * @throws IOException if there is a file open error.
  */
  public static synchronized List<String> getFileLines(String isFileName)
         throws IOException
  {
    List<String> vpLineList = new ArrayList<String>();

    try(FileReader vpFRead = new FileReader(isFileName);
        LineNumberReader vpFileReader = new LineNumberReader(vpFRead))
    {
      for(String vsLine = vpFileReader.readLine(); vsLine != null;
                 vsLine = vpFileReader.readLine())
      {
        if (!vsLine.startsWith("#") && vsLine.trim().length() != 0) vpLineList.add(vsLine);
      }
    }
    catch(FileNotFoundException e)
    {
      throw new IOException("File " + isFileName + " not found on the system..." + e.getMessage());
    }

    return(vpLineList);
  }

 /**
  * Method to load contents of a newline delimited file into a string array.  Any
  * line beginning with a # sign or blank lines are ignored.
  * @param ipFileURL a URL representation of file location.
  * @return List of the lines in the file. Empty array if no lines are
  *         read from a file.
  * @throws IOException if there is a resource location error.
  */
  public static synchronized List<String> getFileLines(URL ipFileURL)
         throws IOException
  {
    List<String> vpLineList = new ArrayList<String>();

    BufferedReader vpFileReader = new BufferedReader(new InputStreamReader(ipFileURL.openStream()));
    for(String vsLine = vpFileReader.readLine(); vsLine != null;
               vsLine = vpFileReader.readLine())
    {
      if (!vsLine.startsWith("#") && vsLine.trim().length() != 0) vpLineList.add(vsLine);
    }

    return(vpLineList);
  }

 /**
  * Method returns a list of files in a given directory sorted by last
  * modification date.  No subdirectories are returned in the list.
  * @param isDirectory the directory where files reside.  This could be an
  *        absolute or relative path.
  * @param izDescending boolean of ascending or descending time order. <code>true</code>
  *        means sort in descending time order.
  * @return array of files.  Empty array if no entries found.
  */
  public static File[] getFilesSortedByModifyDate(String isDirectory,
                                                  final boolean izDescending)
  {
    File vpDirectory = new File(isDirectory);
    File[] vapFiles = vpDirectory.listFiles(new FileFilter()
    {
      @Override
      public boolean accept(File ipFileObj)
      {
        return(ipFileObj.isFile());
      }
    });

    if (vapFiles == null) return(new File[0]);

    if (vapFiles.length == 0) return(new File[0]);

    Arrays.sort(vapFiles, new Comparator()
    {
      int   vnRtn;
      long  vnFileTime1;
      long  vnFileTime2;

      @Override
      public int compare(Object ipObj1, Object ipObj2)
      {
        vnFileTime1 = ((File)ipObj1).lastModified();
        vnFileTime2 = ((File)ipObj2).lastModified();

        if (izDescending)
          vnRtn = (vnFileTime1 < vnFileTime2)  ? 1 :
                  (vnFileTime1 == vnFileTime2) ? 0 : -1;
        else
          vnRtn = (vnFileTime1 > vnFileTime2)  ? 1 :
                  (vnFileTime1 == vnFileTime2) ? 0 : -1;

        return(vnRtn);
      }
    });

    return(vapFiles);
  }

  /**
   * Rethrows unchecked exception.
   *
   * <p><b>Details:</b> <code>rethrow</code> checks the supplied throwable and
   * does the following:</p>
   *
   * <ol>
   *   <li>Unwrap the throwable if it is an
   *     <code>InvocationTargetException</code>.</li>
   *   <li>If the throwable is an unchecked throwable
   *     (<code>RuntimeException</code> or <code>Error</code>, rethrow it.</li>
   *   <li>Otherwise, return the original or unwrapped throwable.</li>
   * </ol>
   *
   * <p>This method is best used in patterns that employ reflection or
   * synchronous fiber execution.</p>
   *
   * @param ie throwable to process
   * @return original or unwrapped throwable
   * @throws RuntimeException if the exception or cause is a RuntimeException
   * @throws Error if the exception or cause is an Error
   */
  public static Throwable rethrow(Throwable ie)
  {
    if (ie instanceof InvocationTargetException)
      ie = ie.getCause();
    if (ie instanceof RuntimeException)
      throw (RuntimeException) ie;
    if (ie instanceof Error)
      throw (Error) ie;
    return ie;
  }

 /**
  * Method creates one string out of all messages buried inside an exception trace.
  * @param ipExcep the outermost exception.
  * @return String containing concatinated message.
  */
  public static String appendNestedExceptionMessages(Throwable ipExcep)
  {
    String vsRtnMsg = "";

    if (ipExcep.getCause() != null)
    {
      boolean vzDone = false;
      while(!vzDone)
      {
        Throwable vpExcep = ipExcep.getCause();
        ipExcep = vpExcep;
        if (vpExcep == null)
        {
          vzDone = true;
          continue;
        }

        if (vpExcep.getMessage() != null && !vpExcep.getMessage().isEmpty())
        {
          vsRtnMsg += (vpExcep.getMessage() + " ");
          vzDone = true;
        }
      }
    }

    return(createParagraph(vsRtnMsg, 40));
  }

 /**
  * Method takes a long string and inserts newlines at critical points so that the
  * resulting string is more readable for displays.  This method will respect
  * word and punctuation boundaries.
  *
  * @param isText the text to break up.
  * @param inLineLength the number of chars. before a newline should be
  *        inserted.  <b>Note: </b> If the situation arises where a newline is
  *        to be inserted in the middle of a word, the newline will be inserted
  *        previous to this word.
  *
  * @return string with newlines inserted.
  */
  public static String createParagraph(String isText, int inLineLength)
  {
    String vsRtn = "";
    if (isText.isEmpty()) return("");

    try(Scanner vpWordScanner = new Scanner(isText))
    {
      vpWordScanner.useDelimiter("\\p{Blank}+");

      StringBuilder vpNewLinedText = new StringBuilder();
      StringBuilder vpTempBuf = new StringBuilder();

      while(vpWordScanner.hasNext())
      {
        String vsWord = vpWordScanner.next();
        vpTempBuf.append(vsWord);

        int vnAccumulatedLen = vpTempBuf.length();
        if (vnAccumulatedLen == inLineLength)
        {
          vpNewLinedText.append(vsWord).append(SKDCConstants.EOL_CHAR);
          vpTempBuf.setLength(0);
        }
        else if (vnAccumulatedLen > inLineLength)
        {
          vpNewLinedText.append(SKDCConstants.EOL_CHAR).append(vsWord).append(" ");
          vpTempBuf.setLength(0);
          vpTempBuf.append(vsWord).append(" ");
        }
        else
        {
          vpNewLinedText.append(vsWord).append(" ");
          vpTempBuf.append(" ");
        }
      }
      vsRtn = vpNewLinedText.toString();
    }

    return vsRtn;
  }

 /**
  * Method to return an Integer Array for a primitive variable argument -- in
  * this case integers.  This is to work around the problem of auto-boxing not
  * working for primitive arrays (primitive var. args).
  *
  * @param ianIntegers variable list of primitive integers.
  * @return
  */
  public static Integer[] getVarArgIntegerArray(int... ianIntegers)
  {
    Integer[] vapInts = new Integer[ianIntegers.length];

    for(int vnIdx = 0; vnIdx < ianIntegers.length; vnIdx++)
    {
      vapInts[vnIdx] = Integer.valueOf(ianIntegers[vnIdx]);
    }

    return(vapInts);
  }

  /**
   * Helper method to check for data in a string
   *
   * @param s
   * @return
   */
  public static boolean isFilledIn(String s)
  {
    return ((s != null) && (s.length() > 0));
  }
  
  /*====================================================================*/
  /* Helper methods inspired by Apache Lang3 StringUtils.  Someday      */
  /* maybe we'll just include Apache's library. [START]                 */
  /*====================================================================*/
  /**
   * String.equals() with built in null checking
   * 
   * @param s1
   * @param s2
   * @return
   */
  public static boolean equals(String s1, String s2)
  {
    if (s1 == s2)
    {
      return true;
    }
    if (s1 == null || s2 == null)
    {
      return false;
    }
    return s1.equals(s2);
  }

  /**
   * Is the string blank/null?
   * 
   * @param s
   * @return true is null or all whitespace, false otherwise
   */
  public static boolean isBlank(String s)
  {
    return (s == null) || (s.trim().length() == 0);
  }

  /**
   * Is the string not blank/null?
   * 
   * @param s
   * @return
   */
  public static boolean isNotBlank(String s)
  {
    return !isBlank(s);
  }

  /**
   * String.startsWith() with built-in null checking
   * 
   * @param source
   * @param prefix
   * @return
   */
  public static boolean startsWith(String source, String prefix)
  {
    if (source == null || prefix == null)
    {
      return source == prefix;
    }
    if (prefix.length() > source.length())
    {
      return false;
    }
    return source.startsWith(prefix);
  }
  
  /**
   * Substring with built-in null & range checking
   * 
   * @param s
   * @param start
   * @param end
   * @return
   */
  public static String substring(String s, int start, int end)
  {
    if (s == null)
    {
      return null;
    }
    if (end > s.length())
    {
      end = s.length();
    }
    if (start > end)
    {
      return "";
    }
    return s.substring(start, end);
  }

  /*====================================================================*/
  /* Helper methods inspired by Apache Lang3 StringUtils.  Someday      */
  /* maybe we'll just include Apache's library. [END]                   */
  /*====================================================================*/
  
  /**
   * Sleep without an exception if interrupted
   * 
   * @param millis
   */
  public static void sleep(long millis)
  {
    try
    {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {}
  }

}

