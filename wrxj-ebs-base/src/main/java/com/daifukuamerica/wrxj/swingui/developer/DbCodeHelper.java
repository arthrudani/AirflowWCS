package com.daifukuamerica.wrxj.swingui.developer;

import com.daifukuamerica.wrxj.jdbc.DBException;

/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$
  
  Copyright (c) 2015 Wynright Corporation.  All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/

import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCCheckBox;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Attempt to avoid writing tons of boilerplate code for new database tables
 *  
   INSERT INTO ROLEOPTION (SROLE, SCATEGORY, SOPTION, SICONNAME, SCLASSNAME, IBUTTONBAR, IADDALLOWED, IMODIFYALLOWED, IDELETEALLOWED, IVIEWALLOWED)
   VALUES ('SKDaifuku', 'Developer', 'DB to Java', '/graphics/dbToJava.png', 'developer.DbCodeHelper', 1, 1, 1, 1, 1);
 *
 * @author mandrus
 */
public class DbCodeHelper extends DacInputFrame
{
  private static final long serialVersionUID = 1L;
  
  private SKDCTextField mpDBConfig;
  private SKDCCheckBox mpDBHasHungarianNotation;
  
  public DbCodeHelper()
  {
    this("DB Code Generator");
  }
  
  /**
   *  Create container screen class.
   *
   *  @param isTitle Title to be displayed.
   */
  public DbCodeHelper(String isTitle)
  {
    super(isTitle, "DB Code Generator");
    try
    {
      buildScreen();
    }
    catch (Exception nsfe)
    {
      logger.logException(nsfe);
      displayError(nsfe.getMessage());
    }
  }

  /*========================================================================*/
  /*  Methods for display formatting                                        */
  /*========================================================================*/
  /**
   * Builds the update form
   * 
   * @throws NoSuchFieldException
   */
  protected void buildScreen() throws NoSuchFieldException
  {
    // Search Panel
    getContentPane().remove(mpWarningPanel);
    JPanel vpSearchPanel = getEmptyButtonPanel();
    vpSearchPanel.add(new SKDCLabel("Database Config:"));
    mpDBConfig = new SKDCTextField(20);
    mpDBConfig.setToolTipText("The name of the database configuration from the properties file.");
    vpSearchPanel.add(mpDBConfig);
    mpDBHasHungarianNotation = new SKDCCheckBox("Hungarian Columns");
    mpDBHasHungarianNotation.setToolTipText("Check if the first character of the column names should be ignored for naming purposes.");
    vpSearchPanel.add(mpDBHasHungarianNotation);
    SKDCButton vpSearchButton = new SKDCButton("Get Database Info");
    vpSearchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        try {
          refreshTables();
        } catch (DBException dbe) {
          dbe.printStackTrace();
          setError("Error connecting to " + mpDBConfig.getText() + " (see console)");
        }
      }
    });
    vpSearchPanel.add(vpSearchButton);
    
    JPanel vpNewTopPanel = new JPanel(new BorderLayout());
    vpNewTopPanel.add(vpSearchPanel, BorderLayout.CENTER);
    vpNewTopPanel.add(mpWarningPanel, BorderLayout.SOUTH);
    getContentPane().add(vpNewTopPanel, BorderLayout.NORTH);

    // Center panel
    JScrollPane sp = new JScrollPane(mpInputPanel);
    sp.setPreferredSize(new Dimension(900, 500));
    sp.getVerticalScrollBar().setUnitIncrement(30);
    mpCenterPanel.remove(mpInputPanel);
    mpCenterPanel.add(sp, BorderLayout.NORTH);
    setResizable(true);
    
    // Button Panel
    mpBtnSubmit.setVisible(false);
    mpBtnClear.setVisible(false);
  }
  
  /**
   * Build the table list
   */
  protected void refreshTables() throws DBException {

    mpInputPanel.removeAll();

    DBTableInfo vpTableInfo = new DBTableInfo(mpDBConfig.getText(), null);

    List<String> tables = new ArrayList<>();
    tables.addAll(vpTableInfo.getTables());
    Collections.sort(tables);
    
    for (String table : tables) {
      JPanel bPanel = new JPanel();
      SKDCTextField vpTableName = new SKDCTextField(25, 50);
      vpTableName.setText(DBTableInfo.suggestObjName(table));
      bPanel.add(vpTableName);
      bPanel.add(new SKDCButton(new DbAction("Enum",
          new CodeGeneratorDbEnum(vpTableInfo), table, vpTableName)));
      bPanel.add(new SKDCButton(new DbAction("Data",
          new CodeGeneratorDbData(vpTableInfo), table, vpTableName)));
      bPanel.add(new SKDCButton(new DbAction("DBInt",
          new CodeGeneratorDbInt(vpTableInfo), table, vpTableName)));
      addInput(table, bPanel);
    }
    setInfo("Loaded database information");
    validate();
  }
  
  /*========================================================================*/
  /*  Action handling                                                       */
  /*========================================================================*/
  private class DbAction extends AbstractAction {
    private static final long serialVersionUID = 6191436194641014480L;
    
    private CodeGeneratorDB codeGenerator;
    private String dbTableName;
    private SKDCTextField javaObjectName;
    
    public DbAction(String btnLabel, CodeGeneratorDB codeGenerator,
        String dbTableName, SKDCTextField javaObjectName)
    {
      super(btnLabel);
      this.codeGenerator = codeGenerator;
      this.dbTableName = dbTableName;
      this.javaObjectName = javaObjectName;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae)
    {
      try
      {
        setInfo("Generating " + codeGenerator.getDescription() + " file...");
        codeGenerator.setUsesHungarianNotationColumnNames(
            mpDBHasHungarianNotation.isSelected());
        setInfo("Generated " + codeGenerator.generateCode(dbTableName,
            javaObjectName.getText()));
      }
      catch (Exception ex)
      {
        logger.logException(ex);
        setError(ex.getClass() + ":" + ex.getMessage());
      }
    }
  }

  /**
   * Action method to handle Close button.
   */
  @Override
  protected void closeButtonPressed()
  {
    close();
  }
}
