package com.daifukuamerica.wrxj.printer;

import com.daifukuamerica.wms.printer.DacLabelGenerator;
import com.daifukuamerica.wms.printer.barcode.LabelGeneratorException;
import com.daifukuamerica.wms.printer.logging.LabelPrinterLogger;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.swing.DacInputFrame;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

/**
 * Test GUI for barcode printing subsystem.
 * 
 * <p><b>Details:</b> <code>SkdLabelGeneratorFrame</code> provides a GUI to 
 * allow a user to test configured barcode printers using the
 * <code>DacLabelGenerator</code> class.</p>
 */
@SuppressWarnings("serial")
public class SkdLabelGeneratorFrame extends DacInputFrame
{
  LabelPrinterLogger mpLogger = new DacLabelPrinterLoggerWRx();
  
  /**
   * Class to handle the underlying data for our mpFieldTable
   */
  private class LabelTableModel extends AbstractTableModel
  {
    private String[][] masData;
    public boolean mbReadOnlyFieldNames = false;

    /**
     * Constructor
     */
    public LabelTableModel()
    {
      super();
      masData = new String[2][5];
    }

    /**
     * Get the column count
     * 
     * @return int
     */
    @Override
    public int getColumnCount()
    {
      return 2;
    }

    /**
     * Get the column names
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     * 
     * @param inColumn
     * @return String
     */
    @Override
    public String getColumnName(int inColumn)
    {
      if (inColumn == 0)
      {
        return "Field";
      }
      else
      {
        return "Data";
      }
    }

    /**
     * Get the number of rows
     * 
     * @return int
     */
    @Override
    public int getRowCount()
    {
      return masData[0].length;
    }

    /**
     * Get the value of a cell
     * 
     * @param inRow
     * @param inColumn
     * @return String
     */
    @Override
    public String getValueAt(int inRow, int inColumn)
    {
      return masData[inColumn][inRow];
    }

    /**
     * Is a cell editable?
     * 
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     * 
     * @param inRow
     * @param inColumn
     * @return boolean
     */
    @Override
    public boolean isCellEditable(int inRow, int inColumn)
    {
      if (mbReadOnlyFieldNames)
      {
        return (inColumn == 1);
      }
      else
      {
        return (true);
      }
    }

    /**
     * Set the row count
     * 
     * @param inRow
     */
    public void setRowCount(int inRow)
    {
      masData = new String[2][inRow];
      mbReadOnlyFieldNames = false;
      fireTableDataChanged();
    }

    /**
     * Clear the table
     */
    public void clear()
    {
      setRowCount(5);
    }

    /**
     * Set the value of a cell
     * 
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
     *      int, int)
     *      
     * @param ipValue
     * @param inRow
     * @param inColumn
     */
    @Override
    public void setValueAt(Object ipValue, int inRow, int inColumn)
    {
      if (ipValue.getClass() == String.class)
      {
        masData[inColumn][inRow] = (String)ipValue;
      }
    }
  }

  /*------------------------------------------------------------------------*/

  private LabelTableModel mpFieldData = new LabelTableModel();
  private SKDCButton mpPrintButton = new SKDCButton("Print Label");
  private SKDCButton mpGetFieldsButton = new SKDCButton("Get Fields");
  private JComboBox mpPrinterCombo = new JComboBox();
  private JComboBox mpLabelCombo = new JComboBox();
  private JTable mpFieldTable = new JTable();
  private JScrollPane mpScrollPane = new JScrollPane(mpFieldTable);

  /**
   * Constructor
   */
  public SkdLabelGeneratorFrame()
  {
    super("Barcode Generator", "Barcode Information");
    try
    {
      buildScreen();
    }
    catch (Exception e)
    {
      logAndDisplayException(e);
    }
  }

  /**
   * Fill the printer combo box
   * 
   * @throws Exception
   */
  private void initializePrinterCombo() throws Exception
  {
    // Populate our combo box with the appropriate printers and labels
    DocFlavor vpDocFlavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
    PrintService[] vapPrintService = PrintServiceLookup.lookupPrintServices(
        vpDocFlavor, null);
    for (int vnIndex = 0; vnIndex < vapPrintService.length; vnIndex++)
    {
      mpPrinterCombo.addItem(vapPrintService[vnIndex].getName());
    }
    mpPrinterCombo.setMaximumRowCount(3);
    mpPrinterCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        mpFieldData.clear();
      }
    });
  }

  /**
   * Fill the label combo box
   * 
   * @throws Exception
   */
  private void initializeLabelCombo() throws Exception
  {
    // Get the barcode templates
    String vsTemplates = Application.getString("BarcodeTemplates");
    if (vsTemplates != null && vsTemplates.length() > 0)
    {
      String[] vasTemplates = vsTemplates.split(",");
      for (int vnIndex = 0; vnIndex < vasTemplates.length; vnIndex++)
      {
        mpLabelCombo.addItem(vasTemplates[vnIndex]);
      }
    }
    // Allow the user to specify a label that is not configured
    mpLabelCombo.setEditable(true);
    mpLabelCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        mpFieldData.clear();
      }
    });
  }

  /**
   * Initialize the table
   */
  private void initializeTable()
  {
    // Table
    mpFieldTable.setModel(mpFieldData);
    mpFieldTable.setCellSelectionEnabled(true);

    // get the default tab action so that we can call it
    InputMap vpInputMap = mpFieldTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke vpTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    final Action vpDefaultTab = mpFieldTable.getActionMap().get(
        vpInputMap.get(vpTab));

    vpInputMap.put(vpTab, "customTab");
    mpFieldTable.getActionMap().put("customTab", new AbstractAction() {
      public void actionPerformed(ActionEvent e)
      {
        vpDefaultTab.actionPerformed(e);
        customTab();
      }
    });
  }

  /**
   * Initialize the buttons
   */
  private void initializeButtons()
  {
    mpGetFieldsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        getFieldsButtonPressed();
      }
    });

    mpPrintButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        printLabelButtonPressed();
      }
    });
    
    mpBtnSubmit.setVisible(false);
  }

  /**
   * Build the screen
   * 
   * @throws Exception
   */
  private void buildScreen() throws Exception
  {
    setPreferredSize(new Dimension(410, 400));
    setMinimumSize(new Dimension(200, 300));

    initializePrinterCombo();
    initializeLabelCombo();
    initializeTable();
    initializeButtons();

    addInput("Printer Name", mpPrinterCombo);
    addInput("Label Name", mpLabelCombo);

    mpCenterPanel.add(mpScrollPane, BorderLayout.CENTER);

    mpButtonPanel.add(mpGetFieldsButton);
    mpButtonPanel.add(mpBtnClear);
    mpButtonPanel.add(mpPrintButton);
    mpButtonPanel.add(mpBtnClose);
  }

  /**
   * Tab action for the table
   */
  private void customTab()
  {
    int vnRow = mpFieldTable.getSelectedRow();
    int vnColumn = mpFieldTable.getSelectedColumn();
    while (!mpFieldTable.isCellEditable(vnRow, vnColumn))
    {
      vnColumn += 1;
      if (vnColumn == mpFieldTable.getColumnCount())
      {
        vnColumn = 0;
        vnRow += 1;
      }
      if (vnRow == mpFieldTable.getRowCount())
      {
        vnRow = 0;
      }
      if (vnRow == mpFieldTable.getSelectedRow() && 
          vnColumn == mpFieldTable.getSelectedColumn())
      {
        break;
      }
    }
    mpFieldTable.changeSelection(vnRow, vnColumn, false, false);
  }

  /**
   * Get the fields for a label template
   */
  private void getFieldsButtonPressed()
  {
    try
    {
      List<String> vpList = DacLabelGenerator.getTemplateFields(
          (String)mpPrinterCombo.getSelectedItem(),
          (String)mpLabelCombo.getSelectedItem(), mpLogger);
      if (vpList != null)
      {
        mpFieldData.setRowCount(vpList.size());
        for (int vpIndex = 0; vpIndex < vpList.size(); vpIndex++)
        {
          mpFieldData.setValueAt(vpList.get(vpIndex), vpIndex, 0);
        }
        mpFieldData.mbReadOnlyFieldNames = true;
        mpFieldTable.requestFocusInWindow();
      }
      else
      {
        mpFieldData.clear();
      }
    }
    catch (Exception ve)
    {
      if (ve.getClass() == LabelGeneratorException.class)
      {
        displayInfo("Unable to locate template.");
      }
      else
      {
        ve.printStackTrace();
      }
    }
  }

  /**
   * Clear
   */
  @Override
  protected void clearButtonPressed()
  {
    if (mpFieldTable.getEditingColumn() != -1)
    {
      mpFieldTable.getCellEditor().cancelCellEditing();
    }

    for (int vnIndex = 0; vnIndex < mpFieldData.getRowCount(); vnIndex++)
    {
      mpFieldData.setValueAt("", vnIndex, 1);
    }
    mpFieldTable.repaint();
    mpFieldTable.requestFocusInWindow();
  }

  /**
   * Print the label
   */
  protected void printLabelButtonPressed()
  {
    if (mpFieldTable.getEditingColumn() != -1)
    {
      mpFieldTable.getCellEditor().stopCellEditing();
    }

    HashMap<String, String> vpLabelDataMap = new HashMap<String, String>();
    for (int vpIndex = 0; vpIndex < mpFieldData.getRowCount(); vpIndex++)
    {
      vpLabelDataMap.put(mpFieldData.getValueAt(vpIndex, 0),
          mpFieldData.getValueAt(vpIndex, 1));
    }

    try
    {
      DacLabelGenerator.print((String)mpPrinterCombo.getSelectedItem(),
          (String)mpLabelCombo.getSelectedItem(), vpLabelDataMap, mpLogger);
    }
    catch (Exception ve)
    {
      ve.printStackTrace();
    }
  }
}