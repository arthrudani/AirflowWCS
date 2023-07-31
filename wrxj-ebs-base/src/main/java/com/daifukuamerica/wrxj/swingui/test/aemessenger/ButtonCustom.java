/* ***************************************************************************
  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swingui.test.aemessenger;

import com.daifukuamerica.wrxj.controller.aemessenger.process.json.JsonAeMessageRequest;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.swing.JTextArea;

/**
 * Button for testing XXX -> WRx messages with custom messages.
 * @author mandrus
 */
public class ButtonCustom extends AEMessengerButton
{
  private static final long serialVersionUID = -6711506432504074814L;
  
  String msMessage;

  public ButtonCustom(JsonAeMessageRequest vpRequest, SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld, int inProduct, int inDefaultInstance)
  {
    super(vpRequest.getMessageType(), ipSourceFld, ipMsgFld);
    
    initSource(inProduct, inDefaultInstance);

    try
    {
      Method[] allMethods = vpRequest.getClass().getDeclaredMethods();
      for (Method method : allMethods)
      {
        if (!method.getName().equalsIgnoreCase("setMessageType")
            && method.getName().startsWith("set"))
        {
          if (method.getName().contains("Date"))
          {
            method.invoke(vpRequest, "MM/dd/yy");
          }
          else if (method.getParameterTypes()[0].isAssignableFrom(String.class))
          {
            method.invoke(vpRequest, method.getName().substring(3));
          }
          else if (method.getParameterTypes()[0].isAssignableFrom(char.class))
          {
            method.invoke(vpRequest, method.getName().charAt(3));
          }
          else if (method.getParameterTypes()[0].isAssignableFrom(Integer.class))
          {
            method.invoke(vpRequest, 0);
          }
          else if (method.getParameterTypes()[0].isAssignableFrom(int.class))
          {
            method.invoke(vpRequest, 0);
          }
          else if (method.getParameterTypes()[0].isAssignableFrom(Boolean.class))
          {
            method.invoke(vpRequest, false);
          }
          else if (method.getParameterTypes()[0].isEnum())
          {
            method.invoke(vpRequest, method.getParameterTypes()[0].getEnumConstants()[0]);
          }
          else if (method.getParameterTypes()[0].isArray())
          {
            method.invoke(vpRequest, new Object[] { new String[] {"Arg1","Arg2","Arg3"} });
          }
        }
      }
      
      msMessage = new GsonBuilder().setPrettyPrinting().create().toJson(vpRequest);
    }
    catch (Exception e)
    {
      msMessage = Arrays.toString(e.getStackTrace());
    }
  }

  @Override
  protected String getMessage()
  {
    return msMessage;
  }
}
