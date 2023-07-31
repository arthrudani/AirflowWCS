/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import com.daifukuamerica.wrxj.swing.SKDCTextField;
import com.daifukuamerica.wrxj.swingui.developer.DBTableInfo;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * <B>Description:</B> Warehouse Rx Table Data Exporter
 * <br/>Configurations:
 * <ul>
 * <li>DBExporter.ActiveTables</li>
 * </ul>
 * 
 * <p>
   INSERT INTO ROLEOPTION (SROLE, SCATEGORY, SOPTION, SICONNAME, SCLASSNAME, IBUTTONBAR, IADDALLOWED, IMODIFYALLOWED, IDELETEALLOWED, IVIEWALLOWED)
   VALUES ('SKDaifuku', 'Developer', 'DB Exporter', '/graphics/dbExport.png', 'utility.DBExportFrame', 1, 1, 1, 1, 1);
 * </p>
 *
 * @author       mandrus
 * @version      1.0
 */
public class DBExportFrame extends SKDCInternalFrame
{
  private static final long serialVersionUID = 6296779016041862174L;

  private static final String SELECT_ALL = "Select All";
  private static final String SELECT_ACTIVE = "Select Active";
  private static final String[] ACTIVE_TABLES;
  static
  {
    String vsActiveTables = Application.getString("DBExporter.ActiveTables");
    if (SKDCUtility.isNotBlank(vsActiveTables))
    {
      ACTIVE_TABLES = vsActiveTables.split(",");
    }
    else
      ACTIVE_TABLES = new String[0];
  }
  
  private static final String ORACLE = "Oracle";
  private static final String SQLSERVER = "SQL Server";
  
  // Source Database
  private SKDCTextField mpDBConfig, mpDBSchema;
  
  // Source Database Tables
  private JPanel mpTablePanel;
  private List<JCheckBox> mpTableCheckBoxes; 
  
  // Output File
  private JComboBox<String> mpDialectCombo;

  // Progress & Feedback
  private JProgressBar mpProgressBar;
  protected JTextArea mpFeedback;

  /*========================================================================*/
  /* Constructors                                                           */
  /*========================================================================*/
  
  /**
   * Constructor
   * 
   * @param isTitle
   * @param izResizable
   * @param izClosable
   */
  public DBExportFrame(String isTitle, boolean izResizable, boolean izClosable)
  {
    super(isTitle, izResizable, izClosable);
    setMaximizable(true);
    buildScreen();
  }

  /**
   * Constructor
   * 
   * @param isTitle
   */
  public DBExportFrame(String isTitle)
  {
    this(isTitle, true, true);
  }

  /**
   * Constructor
   */
  public DBExportFrame()
  {
    this("DB Table Export", true, true);
  }

  /*========================================================================*/
  /* Button pressed action methods                                          */
  /*========================================================================*/
  /**
   * (De)select All
   * @param izSelect
   */
  public void selectAll(boolean izSelect)
  {
    mpFeedback.setText("");
    for (JCheckBox cb : mpTableCheckBoxes)
    {
      if (!cb.getText().equals(SELECT_ALL))
        cb.setSelected(izSelect);
    }
  }

  /**
   * (De)select Active
   * @param izSelect
   */
  public void selectActive(boolean izSelect)
  {
    mpFeedback.setText("");
    for (JCheckBox cb : mpTableCheckBoxes)
    {
      for (String vsTable : ACTIVE_TABLES)
      {
        if (cb.getText().equals(vsTable))
        {
          cb.setSelected(izSelect);
          break;
        }
      }
    }
  }

  /**
   * Export
   */
  public void runExport()
  {
    initializeTextArea();
    
    // Determine which DB Exporter to use
    DBExporter vpExporter = getDBExporter();
    if (vpExporter == null)
    {
      appendError("DBExporter for ["
          + mpDialectCombo.getSelectedItem().toString() + "] not found!");
      return;
    }
    appendInfo(vpExporter.getRenamingReport());

    // Export!
    mpProgressBar.setMaximum(mpTableCheckBoxes.size());
    mpProgressBar.setMinimum(0);
    mpProgressBar.setValue(0);
    new Thread() {
      @Override
      public void run()
      {
        for (JCheckBox cb : mpTableCheckBoxes)
        {
          if (!cb.getText().equals(SELECT_ALL)
              && !cb.getText().equals(SELECT_ACTIVE)
              && cb.isSelected())
          {
            try
            {
              appendInfo("Exporting " + cb.getText() + "... ", false);
              int vnCount = vpExporter.export(cb.getText());
              appendInfo("exported " + vnCount + " rows");
            }
            catch (Exception e)
            {
              logger.logException("Error exporting " + cb.getText(), e);
              appendError(e.getMessage());
            }
          }
          mpProgressBar.setValue(mpProgressBar.getValue()+1);
        }
        appendInfo("Export complete.  Files are in " + vpExporter.getOutputPath());
      }
    }.start();
  }
  
  /**
   * Factory for DBExporter
   * @return
   */
  protected DBExporter getDBExporter()
  {
    switch (mpDialectCombo.getSelectedItem().toString())
    {
      case ORACLE:
        return Factory.create(DBExporterOracle.class, mpDBConfig.getText());
      case SQLSERVER:
        return Factory.create(DBExporterSqlServer.class, mpDBConfig.getText());
      default:
        return null;
    }
  }

  /*========================================================================*/
  /* Feedback                                                               */
  /*========================================================================*/
  /**
   * Initialize the text area
   */
  protected void initializeTextArea()
  {
    mpFeedback.setText("");
  }

  /**
   * Append INFO
   * @param isMessage
   * @param izEOL
   */
  protected void appendInfo(String isMessage)
  {
    appendInfo(isMessage, true);
  }

  /**
   * Append INFO
   * @param isMessage
   * @param izEOL
   */
  protected void appendInfo(String isMessage, boolean izEOL)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run()
      {
        mpFeedback.append(isMessage);
        if (izEOL)
          mpFeedback.append(System.lineSeparator());
      }
    });
  }

  /**
   * Append ERROR
   * @param isMessage
   */
  protected void appendError(String isMessage)
  {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run()
      {
        getToolkit().beep();
        mpFeedback.append(System.lineSeparator() + "*** " + isMessage + " ***"
            + System.lineSeparator());
      }
    });
  }

  /**
   * Append ERROR
   * @param isMessage
   */
  protected void appendError(String isMessage, Throwable t)
  {
    logger.logException(isMessage, t);
    appendError(isMessage + " (" + t.getMessage() + "). See log.");
  }

  /*========================================================================*/
  /* Screen Builder Methods                                                 */
  /*========================================================================*/
  private void buildScreen()
  {
    // Assemble screen panels
    Container cp = getContentPane();
    
    // Source Database
    cp.add(buildSourceDatabaesPanel(), BorderLayout.NORTH);
    
    // Center Panels - Source Tables, Feedback
    // Build feedback first so it can list feedback from the table listing
    JPanel vpFeedback = buildFeedbackPanel();
    JPanel vpCenterPanel = new JPanel();
    vpCenterPanel.setLayout(new BoxLayout(vpCenterPanel, BoxLayout.Y_AXIS));
    vpCenterPanel.add(buildSourceTablesPanel());
    vpCenterPanel.add(vpFeedback);
    cp.add(vpCenterPanel);
    
    // South - Buttons
    cp.add(buildButtonPanel(), BorderLayout.SOUTH);
  }

  /**
   * Build the Source Database panel
   * @return
   */
  private JPanel buildSourceDatabaesPanel()
  {
    mpDBConfig = new SKDCTextField(20);
    mpDBConfig.setToolTipText("The name of the database configuration from the properties file.");
    
    mpDBSchema = new SKDCTextField(10);
    mpDBSchema.setToolTipText("The schema to export.");
    mpDBSchema.setText(Application.getString("database.schema", "ASRS"));
    
    SKDCButton vpSearchButton = new SKDCButton("Get Database Info");
    vpSearchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        initializeTextArea();
        buildSourceTablesPanel();
        validate();
        pack();
      }
    });
    
    JPanel vpConfigPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    vpConfigPanel.setBorder(BorderFactory.createTitledBorder("Source Database"));
    vpConfigPanel.add(new SKDCLabel("Database Config:"));
    vpConfigPanel.add(mpDBConfig);
    vpConfigPanel.add(new SKDCLabel("Schema:"));
    vpConfigPanel.add(mpDBSchema);
    vpConfigPanel.add(vpSearchButton);
    return vpConfigPanel;
  }
  
  /**
   * Define input panel components.
   * 
   * @return Built JPanel with input text boxes.
   */
  private JPanel buildSourceTablesPanel()
  {
    // Check Boxes for Tables
    mpTableCheckBoxes = new ArrayList<JCheckBox>();
    
    // Select All option
    JCheckBox vpSelectAll = new JCheckBox(SELECT_ALL, false);
    vpSelectAll.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        selectAll(vpSelectAll.isSelected());
      }
    });
    mpTableCheckBoxes.add(vpSelectAll);

    // Select Active option
    if (ACTIVE_TABLES.length > 0)
    {
      JCheckBox vpSelectActive = new JCheckBox(SELECT_ACTIVE, false);
      vpSelectActive.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e)
        {
          selectActive(vpSelectActive.isSelected());
        }
      });
      mpTableCheckBoxes.add(vpSelectActive);
    }

    // Source Tables
    int vnTableCount = 0;
    List<String> vpViews = new ArrayList<>(); 
    try
    {
      DBTableInfo vpTableInfo = new DBTableInfo(mpDBConfig.getText(),
          mpDBSchema.getText());
      String[] vasTables = vpTableInfo.getTables().toArray(new String[0]);
      Arrays.sort(vasTables);
      for (String vsTable : vasTables)
      {
        if (vsTable.startsWith("V_") || vsTable.endsWith("VIEW"))
        {
          vpViews.add(vsTable);
        }
        else
        {
          mpTableCheckBoxes.add(new JCheckBox(vsTable, false));
          vnTableCount++;
        }
      }
    }
    catch (DBException dbe)
    {
      appendError("Error reading tables", dbe);
    }

    // Add the check boxes to the screen
    int vnRows = (int)Math.ceil((mpTableCheckBoxes.size()) / 4.0);

    if (mpTablePanel == null)
    {
      mpTablePanel = new JPanel(new GridLayout(vnRows, 4));
      mpTablePanel.setBorder(BorderFactory.createTitledBorder("Source Tables"));
    }
    else
    {
      mpTablePanel.removeAll();
      mpTablePanel.setLayout(new GridLayout(vnRows, 4));
    }
    // Alphabetical in rows
//    for (JCheckBox cb : mpTableCheckBoxes)
//    {
//      northPanel.add(cb);
//    }
    // Alphabetical in columns
    for (int i = 0; i < vnRows; i++)
    {
      addCheckBox(mpTablePanel, i);
      addCheckBox(mpTablePanel, i + vnRows);
      addCheckBox(mpTablePanel, i + (vnRows * 2));
      addCheckBox(mpTablePanel, i + (vnRows * 3));
    }
    
    // Feedback
    appendInfo("Loaded " + vnTableCount + " tables.");
    if (vpViews.size() > 0)
    {
      appendInfo("Excluded " + vpViews.size() + " view"
          + (vpViews.size() == 1 ? "" : "s") + ":");
      appendInfo(vpViews.toString()
          .replaceAll("\\[", "  * ")
          .replaceAll("\\]", "")
          .replaceAll(",", "\n  *"));
    }
    return mpTablePanel;
  }
  
  /**
   * Add the check box for the index if the index is valid, or an empty label
   * if not.
   * 
   * @param ipPanel
   * @param inIndex
   */
  private void addCheckBox(JPanel ipPanel, int inIndex)
  {
    if (mpTableCheckBoxes.size() > inIndex)
    {
      ipPanel.add(mpTableCheckBoxes.get(inIndex));
    }
    else
    {
      ipPanel.add(new SKDCLabel(""));
    }
  }

  /**
   * Build the feedback panel
   * @return
   */
  private JPanel buildFeedbackPanel()
  {
    mpProgressBar = new JProgressBar();
    mpFeedback = new JTextArea();
    mpFeedback.setEditable(false);
    JScrollPane vpScroller = new JScrollPane(mpFeedback);
    vpScroller.setPreferredSize(new Dimension(100,100));

    JPanel vpFeedback = new JPanel(new BorderLayout());
    vpFeedback.add(mpProgressBar, BorderLayout.NORTH);
    vpFeedback.add(vpScroller, BorderLayout.CENTER);
    return vpFeedback;
  }
  
  /**
   * Build the button panel at the bottom of the screen.
   * 
   * @return JPanel with buttons in place.
   */
  private JPanel buildButtonPanel()
  {
//    SKDCButton vpBtnSelectAll = new SKDCButton("Select All", "Select All");
//    vpBtnSelectAll.addActionListener(new ActionListener() {
//      @Override
//      public void actionPerformed(ActionEvent e)
//      {
//        selectAll(true);
//      }
//    });
//    
//    SKDCButton vpBtnClear = new SKDCButton("Clear Selections", "Clear Selections");
//    vpBtnClear.addActionListener(new ActionListener() {
//      @Override
//      public void actionPerformed(ActionEvent e)
//      {
//        selectAll(false);
//      }
//    });
    
    mpDialectCombo = new JComboBox<String>(new String[] {ORACLE, SQLSERVER});
    mpDialectCombo.setSelectedItem(SQLSERVER);
    
    SKDCButton vpBtnRunDiagnostic = new SKDCButton("Run Export", "Execute the export tool");
    vpBtnRunDiagnostic.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        runExport();
      }
    });
    
    JPanel vpBtnPanel = getEmptyButtonPanel();
//    vpBtnPanel.add(vpBtnSelectAll);
//    vpBtnPanel.add(vpBtnClear);
    vpBtnPanel.add(new SKDCLabel("Output Dialect:"));
    vpBtnPanel.add(mpDialectCombo);
    vpBtnPanel.add(vpBtnRunDiagnostic);
    return vpBtnPanel;
  }
}
