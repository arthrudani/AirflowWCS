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

import com.daifukuamerica.wrxj.dbadapter.data.aed.Instance;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceCommunications;
import com.daifukuamerica.wrxj.dbadapter.data.aed.InstanceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.JTextArea;

public abstract class AEMessengerButton extends SKDCButton implements ActionListener
{
  private static final long serialVersionUID = -6490281505651895426L;

  private SKDCIntegerField mpLinkedSource;
  private JTextArea mpLinkedMsg;
  private int mnSource;
  
  public AEMessengerButton(String btnlabel, SKDCIntegerField ipSourceFld,
      JTextArea ipMsgFld)
  {
    super(btnlabel);
    mpLinkedSource = ipSourceFld;
    mpLinkedMsg = ipMsgFld;
    addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    mpLinkedSource.setValue(getSource());
    mpLinkedMsg.setText(getMessage());
  }

  protected void initSource(int inProductID, int inDefault)
  {
    try
    {
      List<InstanceData> vpInstances = Factory.create(
          Instance.class).getDataList(inProductID);
      if (vpInstances.isEmpty())
      {
        Logger.getLogger().logError(
            "No sample instance found for " + inProductID);
        mnSource = inDefault;
      }
      else
      {
        InstanceCommunications vpIC = Factory.create(InstanceCommunications.class);
        Map<Integer, String> mpInstanceCommsMap = vpIC.getAeMessengerConnections();
        for (int i = 0; i < vpInstances.size(); i++)
        {
          if (mpInstanceCommsMap.get(vpInstances.get(i).getId()) != null)
          {
            mnSource = vpInstances.get(i).getId();
          }
        }
      }
    }
    catch (Exception e)
    {
      Logger.getLogger().logException(
          "Error looking up sample instance for " + inProductID, e);
      mnSource = inDefault;
    }
  }

  protected int getSource()
  {
    return mnSource;
  }
  
  protected abstract String getMessage();
}
