package com.daifukuamerica.wrxj.swingui.test.toolbox;

import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.swing.TabbedFrame;
import com.daifukuamerica.wrxj.swing.TabbedFramePanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * UI for miscellaneous test helps
 * 
   Insert into ROLEOPTION (SROLE,SCATEGORY,SOPTION,SICONNAME,SCLASSNAME,IBUTTONBAR,IADDALLOWED,IMODIFYALLOWED,IDELETEALLOWED,IVIEWALLOWED,DMODIFYTIME,SADDMETHOD,SUPDATEMETHOD) 
   values ('SKDaifuku','Developer','Toolbox','/graphics/TestTool.png','test.toolbox.TestToolbox',2,1,1,1,1,null,null,null);
 *
 * @author mandrus
 */
public class TestToolbox extends TabbedFrame
{
  private static final long serialVersionUID = -7363238474190411301L;

  /**
   * Constructor
   */
  public TestToolbox()
  {
    super("Test Toolbox");
    setMaximizable(true);
  }

  /**
   * Adds tabs based on SysConfig sGroup=TestToolbox:Panel.
   * All added classes must have a constructor that takes a single 
   * parameter of type GesTestToolbox.  
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void addTabs()
  {
    try
    {
      // Always add instructions
      addTab("Instructions", new InstructionTab(this));
      
      // Configured tabs
      /* To install new tabs: 
      INSERT INTO SYSCONFIG (sGroup,sParameterName,sParameterValue,sDescription) 
        VALUES (
         'TestToolbox:Panel',
         'TAB_NAME',
         'FULL_PACKAGE_NAME.CLASS',
         'TestToolbox Panel')
       */
      Map<String, String> vpPanels = Factory.create(SysConfig.class).getEnabledNameValuePairs("TestToolBox:Panel");
      List<String> vpTabs = new ArrayList<>(vpPanels.keySet());
      Collections.sort(vpTabs, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2)
        {
          return o1.compareToIgnoreCase(o2);
        }});
      for (String vsPanelName: vpTabs)
      {
        try
        {
          Class<TabbedFramePanel> vpClass = (Class<TabbedFramePanel>)Class.forName(vpPanels.get(vsPanelName));
          addTab(vsPanelName, vpClass.getConstructor(TestToolbox.class).newInstance(this));
        }
        catch (Exception e)
        {
          logAndDisplayException("Error loading tab " + vsPanelName, e);
        }
      }
    }
    catch (Exception e)
    {
      logAndDisplayException("Error loading tabs", e);
    }
  }
}
