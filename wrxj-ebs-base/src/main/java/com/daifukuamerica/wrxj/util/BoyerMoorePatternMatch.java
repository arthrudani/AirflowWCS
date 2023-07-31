/*
 * Created on Feb 25, 2004
 *
 */
package com.daifukuamerica.wrxj.util;

import java.util.Locale;

  /**
   * 
   * @author Stephen Kendorski
   *
   * A REALLY FAST search for a string pattern match.  Especially useful
   * when you want to search for the same pattern in many strings and/or
   * the pattern search is case-insensitive.  Also, the longer the pattern
   * string, the quicker the search!
   * 
   * There are a variety of ways to seek a particular string within a string.
   * The simplest is the brute-force method, which works like this. Start at
   * the first character in the target string. Test whether the characters in
   * the string match the characters starting at this point in the target string.
   * If not, try starting at the next character in the target string, and the
   * next, and the next.... The longer the pattern, the longer each step takes.
   * 
   * The brute-force method is especially slow, because it checks the pattern
   * at every possible character position. If we could skip some of those
   * positions, the search would go faster. Well, we can. Suppose the search
   * string is "My Pattern" (ten characters long), and the tenth character of
   * the target string is X, or any character that does not appear in the string
   * at all. The string can't possibly be found at any of the first ten positions,
   * because one of its characters would have to be X. Thus, we can skip ahead
   * by the length of the string. Now suppose the target string character is e.
   * e occurs only once in the string -- as the eighth character. If this is part
   * of an instance of the pattern, that instance must have started seven 
   * haracters back. If that's not the case, we can skip ahead to where that
   * instance would end.
   * 
   * The well-known Boyer-Moore search algorithm, implemented in this class,
   * takes this skipping-ahead technique to its logical extreme. First, it builds
   * a table with a skip value for every possible character. For characters not
   * present in the search string, the skip value is the string's length. For
   * characters that do occur in the string, it's the distance of the character
   * from the string's end. For the last character in the string, the skip value
   * is set to 0. At each step in the process, the algorithm checks the skip value
   * for the character at the current position. If the skip value is anything but
   * zero, the algorithm skips ahead by that amount. If the skip value is zero,
   * the algorithm checks the preceding characters to see if this is the final
   * character in an instance of the search string. Interestingly, the Boyer-Moore
   * search is faster for longer search strings, because longer skips are possible.
   *
   * Upper/Lower Case Note:  This class uses the <code>Character<\code> class to
   * convert characters for case-insensitive matches.  This is not a reliable
   * conversion for many international character sets.  So international match
   * checks may only work reliably as case-sensitive matches.  
   */
public class BoyerMoorePatternMatch
{
  static int alphabetSize = 256;
  private String patternToMatch = null;
  private boolean caseSensitive = false;
  private int patternLength = 0;
  private int[] skipArray = null;
  
  static
  {
    String vsDefaultLanguage = Locale.getDefault().getLanguage();
    if (vsDefaultLanguage != Locale.ENGLISH.getLanguage() )
    {
      alphabetSize = 0x10000;
    }
  }
  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Create a new pattern match object.
   * 
   * @param isPattern text that we will lok for
   * @param ibCaseSensitive if false the match test will ignore case 
   */
  public BoyerMoorePatternMatch(String isPattern, boolean ibCaseSensitive)
  {
    skipArray = new int[alphabetSize];
    patternToMatch = isPattern;
    caseSensitive = ibCaseSensitive;
    patternLength = patternToMatch.length();
    initializePatternSkipTable();
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Take a string, and see if our match pattern appears in the string.
   * 
   * @param isText test to search for pattern
   * @return true if pattern contains text
   */
  public boolean matches(String isText) 
  {
    boolean match = false;
    int vsTextLength = isText.length();
    int pos = patternLength - 1;

    // See if we have a match on the last character
    while (pos < vsTextLength) 
    {
       /* Ordinary character? Skip ahead*/
      if (skipArray[isText.charAt(pos)] > 0)
      {
        pos = pos + skipArray[isText.charAt(pos)];
      }
      else if (skipArray[isText.charAt(pos)] == 0) // Error
      {
        return false;
      }
      else     // We have a match on the last character
      {
        match = true;
        for (int i = pos - patternLength + 1, j = 0; j < patternLength; i++, j++) 
        {
          if (caseSensitive)
          {
            if (isText.charAt(i) != patternToMatch.charAt(j)) 
            { 
              match = false;  
              break;
            }
          }
          else
          {
            //
            // Case INSENSITIVE match
            //
            char vcTextChar = isText.charAt(i);
            vcTextChar = Character.toUpperCase(vcTextChar);
            char vcPatternChar = patternToMatch.charAt(j);
            vcPatternChar = Character.toUpperCase(vcPatternChar);
            if (vcTextChar != vcPatternChar) 
            { 
              match = false;  
              break;
            }
          }
        }
        if (match) 
          return true;
        else 
          pos = pos - skipArray[isText.charAt(pos)];
      }
    }
    return false;
  }

  /*--------------------------------------------------------------------------*/
  // Build a "Skip Table" for the string pattern we want to match.
  private void initializePatternSkipTable()
  {
    //
    // Advance by length of pattern if character is not known.
    //
    for (int i = 0; i < alphabetSize; i++)
    { 
      skipArray[i] = patternLength; // Fill array with length of pattern.
    }
    //
    // Use pattern to fill table with values representing the index/character's
    // distance to the end of the pattern (distance to skip if no match).
    //
    for (int i = 0, skp = patternLength - 1; i < patternLength - 1; i++, skp--)
    { 
      if (caseSensitive)
      {
        skipArray[patternToMatch.charAt(i)] = skp;
      }
      else
      {
        //
        // Match IS Case INSENSITIVE, so update entries for both cases of A-Z.
        //
        char theChar = patternToMatch.charAt(i);
        if (Character.isLowerCase(theChar))
        {
          //
          // Character is lower-case.
          //
          skipArray[theChar] = skp;  // Save lower-case entry.
          theChar = Character.toUpperCase(theChar);     // Convert to upper-case.
          skipArray[theChar] = skp;  // Save upper-case entry.
        }
        else
        {
          skipArray[theChar] = skp;  // Save entry.
          if (Character.isUpperCase(theChar))
          {
            //
            // Character is upper-case.
            //
            theChar = Character.toLowerCase(theChar);     // Convert to lower-case.
            skipArray[theChar] = skp;  // Save lower-case entry.
          }
        }
      }
    }
    //
    // Last character is a special case.
    //
    if (caseSensitive)
    {
      skipArray[patternToMatch.charAt(patternLength - 1)] = -skipArray[patternToMatch.charAt(patternLength-1)];
    }
    else
    {
      //
      // Match IS Case INSENSITIVE, so update entries for both cases of A-Z & a-z.
      //
      char theChar = patternToMatch.charAt(patternLength-1);
      if (Character.isLowerCase(theChar))
      {
        //
        // Character is lower-case.
        //
        skipArray[theChar] = -skipArray[theChar];// Save lower-case entry.
        theChar = Character.toUpperCase(theChar);     // Convert to upper-case.
        skipArray[theChar] = -skipArray[theChar];// Save upper-case entry.
      }
      else
      {
        skipArray[theChar] = -skipArray[theChar];// Save entry.
        if (Character.isUpperCase(theChar))
        {
          //
          // Character is upper-case.
          //
          theChar = Character.toLowerCase(theChar);     // Convert to lower-case.
          skipArray[theChar] = -skipArray[theChar];// Save lower-case entry.
        }
      }
    }
  }
}
