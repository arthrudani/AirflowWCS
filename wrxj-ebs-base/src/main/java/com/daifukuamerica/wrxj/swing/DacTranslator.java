package com.daifukuamerica.wrxj.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * <B>Title:</B> Class to handle Translation Object.<BR>
 * <B>Description:</B> Handles all reading and writing database for Translation<BR>
 *  
 * @author       Michael Andrus
 * 
 * <BR>Copyright (c) 2007 by Daifuku America Corporation
 */
public class DacTranslator
{
  // Yay generics!
  private static Map<String, Set<String>> mpNeededTranslations = 
    new TreeMap<String, Set<String>>();
  
  /*
   * Resource stuff
   */
  private static String msLastLang = "";
  private static Locale mpLocale = null;
  private static ResourceBundle mpResource = null;
  
  /*
   * Default language
   */
  public static String LANG_ENGLISH = "English";
  
  /**
   * Here's a constructor, but all of the methods are static, so...
   */
  public DacTranslator()
  {
    System.out.println("WHY?  All of my methods are static!");
  }

  /*========================================================================*/
  /*  Getters                                                               */
  /*========================================================================*/
  /**
   * Get a list of valid languages.
   * @return List<String> Containing configured langauages
   */
  public static List<String> getLanguages()
  {
    List<String> vpLangList = new ArrayList<String>();
    
    /*
     * Get the translation
     */
    try
    {
      ResourceBundle vpResource = 
        ResourceBundle.getBundle("internationalization/Language");
      for (String s : vpResource.keySet())
      {
        vpLangList.add(s);
      }
      Collections.sort(vpLangList);
    }
    catch (MissingResourceException mre)
    {
      /*
       * If nothing is defined, that's okay.  That just means we're English-only.
       */
    }

    return vpLangList;
  }

  /**
   * Convenience method for compound phrases
   * @param isEnglish
   * @param iasSubs
   * @return
   */
  static public String getTranslation(String isEnglish, String... iasSubs)
  {
    String vsReturn = getTranslation(isEnglish);
    int i = 1;
    for (String s : iasSubs)
    {
      vsReturn = vsReturn.replace("%s" + i, s);
      i++;
    }
    return vsReturn;
  }
  
  /**
   * Get a translation
   * @param isEnglish
   * @return translated string (or isEnglish if not found)
   */
  static public String getTranslation(String isEnglish)
  {
    String vsLanguage = SKDCUserData.getLanguage();
    
    /*
     * If they want English or the string is empty, no translation is necessary
     */
    if (vsLanguage == null || vsLanguage.trim().length() == 0 || isEnglish == null || 
        vsLanguage.equals(LANG_ENGLISH) || isEnglish.trim().length() == 0)
    {
      return isEnglish;
    }

    /*
     * Handle colons
     */
    boolean vzAppendColon = false;
    String vsEnglish = isEnglish.trim();
    if (vsEnglish.endsWith(":"))
    {
      vzAppendColon = true;
      vsEnglish = vsEnglish.substring(0,vsEnglish.length()-1);
    }

    vsEnglish = getTranslationFromResource(vsEnglish, vsLanguage);
    
    return vsEnglish + (vzAppendColon ? ":" : "");
  }
  
  /**
   * Get the translation from the resource file.  If one doesn't exist, return 
   * the original string.
   * @param isEnglish
   * @param isLanguage
   * @return
   */
  private static String getTranslationFromResource(String isEnglish, String isLanguage)
  {
    /*
     * Handle spaces in the key
     */
    String vsEnglish = isEnglish.replace(' ', '_');

    /*
     * Get the language resource to use.
     */
    if (isLanguage == null || isLanguage.trim().length() == 0)
    {
      mpResource = null;
      msLastLang = "";
    }
    else if (!isLanguage.equals(msLastLang))
    {
      try
      {
        ResourceBundle vpResource = 
          ResourceBundle.getBundle("internationalization/Language");
        String isLangCode = vpResource.getString(isLanguage);
        mpLocale = new Locale(isLangCode);
        mpResource = ResourceBundle.getBundle(
            "internationalization/MessagesBundle", mpLocale);
        msLastLang = new String(isLanguage);
      }
      catch (MissingResourceException mre)
      {
        mre.printStackTrace();
      }
    }
    
    /*
     * Don't require internationalization.  If there is no resource, just
     * don't worry about translating.
     */
    if (mpResource == null)
    {
      return isEnglish;
    }
    
    /*
     * Get the translation
     */
    try
    {
      return mpResource.getString(vsEnglish);
    }
    catch (MissingResourceException mre)
    {
//      logger.logDebug(mpLocale.getDisplayLanguage()
//          + " translation not found for \"" + isEnglish + "\"");
//      if (SKDCUserData.getLoginName().equals("su"))
//        System.out.println(vsEnglish);
      trackMissingTranslation(isLanguage, vsEnglish);  
      return new String(isEnglish);
    }
  }
  
  /**
   * Helper method to track missing translations
   * @param isLang
   * @param isKey
   */
  private static void trackMissingTranslation(String isLang, String isKey)
  {
    Set<String> vpMissing = mpNeededTranslations.get(isLang); 
    if (vpMissing == null)
    {
      vpMissing = new TreeSet<String>();
      mpNeededTranslations.put(isLang, vpMissing);
    }
    vpMissing.add(isKey);
  }
  
  /**
   * List all encountered missing translations (in Maps for SKDCTable)
   * @param isLang
   * @return
   */
  public static List<Map<String,String>> listMissingTranslations(String isLang)
  {
    Set<String> vpMissing = mpNeededTranslations.get(isLang);
    List<Map<String,String>> vpReturn = new ArrayList<Map<String,String>>();
    if (vpMissing != null)
    {
      /*
       * Some of the missing translations are actually attempts at 
       * re-translation.  Filter these out.
       */
      ResourceBundle vpResource = 
        ResourceBundle.getBundle("internationalization/Language");
      String isLangCode = vpResource.getString(isLang);
      mpLocale = new Locale(isLangCode);
      mpResource = ResourceBundle.getBundle("internationalization/MessagesBundle", mpLocale);
      msLastLang = new String(isLang);
      
      Enumeration<String> vpKeys = mpResource.getKeys();
      List<String> vpTranslations = new ArrayList<String>();
      while (vpKeys.hasMoreElements())
      {
        String s = mpResource.getString(vpKeys.nextElement()).replace(' ', '_');
        vpTranslations.add(s);
      }
      
      /*
       * Return the missing translations all wrapped up for SKDCTable
       */
      for (String s : vpMissing)
      {
        Map m = new TreeMap<String,String>();
        if (!vpTranslations.contains(s)  &&
            !s.startsWith("Se_encontraron"))  // For spanish lists
        {
          m.put("SKEY", s);
          vpReturn.add(m);
        }
      }
    }
    return vpReturn;
  }
}
