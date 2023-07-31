/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright © 2004 Daifuku America Corporation  All Rights Reserved.
 
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS 
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND 
  CIVIL LIABILITY.
 ****************************************************************************/

package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.swing.table.DacModel;
import com.daifukuamerica.wrxj.swing.table.DacTable;
import com.daifukuamerica.wrxj.swing.table.DacTableMouseListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;

@SuppressWarnings("serial")
public abstract class DacInputFrame extends SKDCInternalFrame
{
  protected JPanel mpCenterPanel = null;
  protected JPanel mpInputPanel = null;
  protected JPanel mpButtonPanel = null;
  protected SKDCButton mpBtnSubmit = null;
  protected SKDCButton mpBtnClear = null;
  protected SKDCButton mpBtnClose = null;

  protected JPanel mpWarningPanel = null;
  private SKDCLabel mpWarningLabel = new SKDCLabel(" ");
  private boolean mzShowWarningPanel = false;
  
  private List<Component> mpLabelList = new ArrayList<Component>();
  private List<Component> mpInputList = new ArrayList<Component>();
  private List<Integer> mpHeightList = new ArrayList<Integer>();

  private int mnHeaderRows   = 0;
  private int mnInputColumns = 1;
  private int mnNextGridY = 0;
  
  public DacInputFrame(String isFrameTitle, String isInputTitle)
  {
    setTitle(isFrameTitle);
    setResizable(false);

    mpWarningPanel = getEmptyButtonPanel();
    mpWarningPanel.setVisible(mzShowWarningPanel);
    mpWarningPanel.add(mpWarningLabel);

    mpCenterPanel = new JPanel(new BorderLayout());
    mpInputPanel = getEmptyInputPanel(isInputTitle);
    mpCenterPanel.add(mpInputPanel, BorderLayout.NORTH);
    
    mpButtonPanel = getEmptyButtonPanel();

    JPanel vpSouthPanel = new JPanel(new BorderLayout());
    vpSouthPanel.add(getInfoPanel(), BorderLayout.CENTER);
    vpSouthPanel.add(mpButtonPanel, BorderLayout.SOUTH);

    addDefaultButtons();
    
    getContentPane().add(mpWarningPanel, BorderLayout.NORTH);
    getContentPane().add(mpCenterPanel, BorderLayout.CENTER);
    getContentPane().add(vpSouthPanel, BorderLayout.SOUTH);
  }
  
  /*========================================================================*/
  /*  Look & Feel methods                                                   */
  /*========================================================================*/
  /**
   * Change button names for add
   */
  protected void useAddButtons()
  {
    mpBtnSubmit.setText("Add");
    mpBtnSubmit.setToolTipText("Add");
    mpBtnSubmit.setMnemonic('A');
  }
  
  /**
   * Change button names for modify
   */
  protected void useModifyButtons()
  {
    mpBtnSubmit.setText("Modify");
    mpBtnSubmit.setToolTipText("Modify");
    mpBtnSubmit.setMnemonic('M');

    mpBtnClear.setText("Reset");
    mpBtnClear.setToolTipText("Reset to defaults");
    mpBtnClear.setMnemonic('R');
  }
  
  /**
   * Change button names for search
   */
  protected void useSearchButtons()
  {
    mpBtnSubmit.setText("Search");
    mpBtnSubmit.setToolTipText("Search");
  }
  
  /**
   * Change button names for search
   */
  protected void useReadOnlyButtons()
  {
    mpBtnSubmit.setVisible(false);
    mpBtnClear.setVisible(false);
    getInfoPanel().setVisible(false);
  }

  /*========================================================================*/
  /*  InputPanel methods                                                    */
  /*========================================================================*/

  /**
   * Add a label/input item to the input panel (1st column)
   * 
   * @param isLabel     - The label to add
   * @param ipComponent - The input to add
   */
  protected void addLabel(String isLabel)
  {
	  addLabel(isLabel, 1);
  }

  /**
   * Add a label/input item to the input panel (1st column)
   * 
   * @param isLabel     - The label to add
   * @param ipComponent - The input to add
   * @param inHeight    - The height in grid blocks of the ipComponent
   */
  protected void addLabel(String isLabel, int inHeight)
  {
    
    addLabel(new SKDCLabel(isLabel), inHeight);
  }

  /**
   * Add a label/input item to the input panel (1st column)
   * 
   * @param ipLabel     - The label to add
   * @param ipInput - The input to add
   */
  protected void addLabel(Component ipLabel)
  {
	  addLabel(ipLabel, 1);
  }
  
  /**
   * Add a label/input item to the input panel (1st column)
   * 
   * @param ipLabel     - The label to add
   * @param ipInput - The input to add
   * @param inHeight    - The height in grid blocks of the ipComponent
   */
  protected void addLabel(Component ipLabel, int inHeight)
  {
    mpLabelList.add(ipLabel);
    mpHeightList.add(Integer.valueOf(inHeight));
    
    if (mnInputColumns == 1)
    {
      GridBagConstraints vpGBC = new GridBagConstraints();
      setLabelColumnGridBagConstraints(vpGBC);
      
      Insets vpOuterLabelInsets = getLabelColumnInsets();
      Insets vpInnerLabelInsets = getInnerLabelColumnInsets();
  
      vpGBC.gridy = mnNextGridY;
      vpGBC.gridheight = inHeight;
      mnNextGridY += inHeight;
      
      vpGBC.gridx = 0;
      vpGBC.anchor = GridBagConstraints.EAST;
      vpGBC.insets = (vpGBC.gridx == 0) ? vpOuterLabelInsets : vpInnerLabelInsets;
      mpInputPanel.add(ipLabel, vpGBC);
    }
    else
    {
      distributeInputsBetweenColumns();
    }
  }
  
  /**
   * Add a label/input item to the input panel (1st column)
   * 
   * @param isLabel     - The label to add
   * @param ipComponent - The input to add
   */
  public void addInput(String isLabel, Component ipComponent)
  {
    addInput(isLabel, ipComponent, 1);
  }

  /**
   * Add a label/input item to the input panel (1st column)
   * 
   * @param isLabel     - The label to add
   * @param ipComponent - The input to add
   * @param inHeight    - The height in grid blocks of the ipComponent
   */
  protected void addInput(String isLabel, Component ipComponent, int inHeight)
  {
    if (isLabel.length() > 0 && !isLabel.endsWith(":"))
    {
      isLabel = isLabel + ":";
    }
    addInput(new SKDCLabel(isLabel), ipComponent, inHeight);
  }

  /**
   * Add a label/input item to the input panel (1st column)
   * 
   * @param ipLabel     - The label to add
   * @param ipInput - The input to add
   */
  protected void addInput(Component ipLabel, Component ipInput)
  {
    addInput(ipLabel, ipInput, 1);
  }
  
  /**
   * Add a label/input item to the input panel (1st column)
   * 
   * @param ipLabel     - The label to add
   * @param ipInput - The input to add
   * @param inHeight    - The height in grid blocks of the ipComponent
   */
  protected void addInput(Component ipLabel, Component ipInput, int inHeight)
  {
    mpLabelList.add(ipLabel);
    mpInputList.add(ipInput);
    mpHeightList.add(Integer.valueOf(inHeight));
    
    if (mnInputColumns == 1)
    {
      GridBagConstraints vpGBC = new GridBagConstraints();
      setLabelColumnGridBagConstraints(vpGBC);
      
      Insets vpOuterLabelInsets = getLabelColumnInsets();
      Insets vpInputInsets      = getInputColumnInsets();
      Insets vpInnerLabelInsets = getInnerLabelColumnInsets();
  
      vpGBC.gridy = mnNextGridY;
      vpGBC.gridheight = inHeight;
      mnNextGridY += inHeight;
      
      vpGBC.gridx = 0;
      vpGBC.anchor = GridBagConstraints.EAST;
      vpGBC.insets = (vpGBC.gridx == 0) ? vpOuterLabelInsets : vpInnerLabelInsets;
      mpInputPanel.add(ipLabel, vpGBC);
        
      vpGBC.gridx = 1;
      vpGBC.anchor = GridBagConstraints.WEST;
      vpGBC.insets = vpInputInsets;
      mpInputPanel.add(ipInput, vpGBC);
    }
    else
    {
      distributeInputsBetweenColumns();
    }
  }
  
  /**
   * Insert a label/input item into the input panel
   * 
   * @param inPosition  - The insertion point (make this the nth input in a 0-based list)
   * @param isLabel     - The label to add
   * @param ipComponent - The input to add
   */
  public void insertInput(int inPosition, String isLabel, Component ipComponent)
  {
    insertInput(inPosition, isLabel, ipComponent, 1);
  }

  /**
   * Insert a label/input item into the input panel
   * 
   * @param inPosition  - The insertion point (make this the nth input in a 0-based list)
   * @param isLabel     - The label to add
   * @param ipComponent - The input to add
   * @param inHeight    - The height in grid blocks of the ipComponent
   */
  protected void insertInput(int inPosition, String isLabel, Component ipComponent, int inHeight)
  {
    if (isLabel.length() > 0 && !isLabel.endsWith(":"))
    {
      isLabel = isLabel + ":";
    }
    insertInput(inPosition, new SKDCLabel(isLabel), ipComponent, inHeight);
  }

  /**
   * Insert a label/input item into the input panel
   * 
   * @param inPosition  - The insertion point (make this the nth input in a 0-based list)
   * @param ipLabel     - The label to add
   * @param ipInput     - The input to add
   */
  protected void insertInput(int inPosition, Component ipLabel, Component ipInput)
  {
    insertInput(inPosition, ipLabel, ipInput, 1);
  }
  
  /**
   * Insert a label/input item into the input panel
   * 
   * @param inPosition  - The insertion point (make this the nth input in a 0-based list)
   * @param ipLabel     - The label to add
   * @param ipInput     - The input to add
   * @param inHeight    - The height in grid blocks of the ipComponent
   */
  protected void insertInput(int inPosition, Component ipLabel, Component ipInput, int inHeight)
  {
    if (inPosition > mpLabelList.size() || inPosition < 0)
    {
      System.err.println(getTitle() + ": Cannot insert input at position " 
          + inPosition + "; list size is " + mpLabelList.size());
      return;
    }
    
    mpLabelList.add(inPosition, ipLabel);
    mpInputList.add(inPosition, ipInput);
    mpHeightList.add(inPosition, Integer.valueOf(inHeight));
    
    distributeInputsBetweenColumns();
  }

  /**
   * Change the title of an input
   * 
   * @param ipInput - the input whose title is to be changed
   * @param isNewTitle - the new title
   */
  protected void updateInputTitle(Component ipInput, String isNewTitle)
  {
    if (isNewTitle.length() > 0 && !isNewTitle.endsWith(":"))
    {
      isNewTitle = isNewTitle + ":";
    }
    for (int i = 0; i < mpInputList.size(); i++)
    {
      Component j = mpInputList.get(i);
      if (j == ipInput)
      {
        j = mpLabelList.get(i);
        if (j instanceof SKDCLabel)
        {
          ((SKDCLabel)j).setText(isNewTitle);
        }
      }
    }
    pack();
  }
  
  /**
   * Convenience method to add vertical whitespace between inputs.
   * <BR>Note: This space increments the input count. 
   * 
   * @param inHeight
   */
  public void addSpace(int inHeight)
  {
    addInput("", Box.createVerticalStrut(inHeight));
  }
  
  /**
   * Convenience method to insert vertical whitespace between inputs
   * <BR>Note: This space increments the input count. 
   * 
   * @param inPosition 
   * @param inHeight
   */
  public void insertSpace(int inPosition, int inHeight)
  {
    insertInput(inPosition, "", Box.createVerticalStrut(inHeight));
  }

  /**
   * Set the number of input columns
   * @param inColumns
   */
  protected void setInputColumns(int inColumns)
  {
    mnInputColumns = inColumns;
    distributeInputsBetweenColumns();
  }
  
  /**
   * Set the number of header rows.
   * <BR>A header row is one which spans all columns and is at the top of the
   * screen.  See UpdateIM and UpdateItemDetail for examples.
   * @param inRows
   */
  protected void setHeaderRows(int inRows)
  {
    mnHeaderRows = inRows;
    distributeInputsBetweenColumns();
  }

  /**
   * Add inputs in multiple columns
   */
  private void distributeInputsBetweenColumns()
  {
    GridBagConstraints vpGBC = new GridBagConstraints();
    setLabelColumnGridBagConstraints(vpGBC);
    
    Insets vpOuterLabelInsets = getLabelColumnInsets();
    Insets vpOuterInputInsets = getInputColumnInsets();
    Insets vpInnerLabelInsets = getLabelColumnInsets();
    Insets vpInnerInputInsets = getInputColumnInsets();

    vpInnerLabelInsets.left = vpInnerLabelInsets.left/2;
    vpInnerInputInsets.right = vpInnerInputInsets.right/2;

    int vnNeededRows = 0;
    for (int i = mnHeaderRows; i < mpInputList.size(); i++)
    {
      Component jc = mpInputList.get(i);
      if (jc.isVisible())
        vnNeededRows += mpHeightList.get(i);
    }
    
    int vnRows = (vnNeededRows / mnInputColumns);
    if ((vnNeededRows % mnInputColumns) > 0)
    {
      vnRows += 1;
    }
    /*
     * Add header rows
     */
    int vnGridX = 0;
    vpGBC.gridy = 0;
    for (int i = 0; i < mnHeaderRows; i++)
    {
      if (!mpInputList.get(i).isVisible()) // Why would a header row be invisible?
        continue;
      
      vpGBC.gridheight = 1;  // In the header rows, all heights are effectively one.

      vpGBC.gridx = vnGridX;
      vpGBC.anchor = GridBagConstraints.EAST;
      vpGBC.insets = (vpGBC.gridx == 0) ? vpOuterLabelInsets : vpInnerLabelInsets;
      mpInputPanel.add(mpLabelList.get(i), vpGBC);
      
      vpGBC.gridx = vnGridX+1;
      vpGBC.anchor = GridBagConstraints.WEST;
      vpGBC.insets = (vpGBC.gridx == (mnInputColumns*2-1)) ? vpOuterInputInsets : vpInnerInputInsets;
      vpGBC.gridwidth = (mnInputColumns * 2) - 1;
      mpInputPanel.add(mpInputList.get(i), vpGBC);

      vpGBC.gridwidth = 1;
      vpGBC.gridy += vpGBC.gridheight;
    }
    
    /*
     * Add other rows
     */
    vnGridX = 0;
    int vnVisible = 0;
    vpGBC.gridy = mnHeaderRows;
    for (int i = mnHeaderRows; i < mpLabelList.size(); i++)
    {
      if (!mpInputList.get(i).isVisible())
        continue;

      vpGBC.gridheight = (mpHeightList.get(i)).intValue();

      vpGBC.gridx = vnGridX;
      vpGBC.anchor = GridBagConstraints.EAST;
      vpGBC.insets = (vpGBC.gridx == 0) ? vpOuterLabelInsets : vpInnerLabelInsets;
      
      mpInputPanel.add(mpLabelList.get(i), vpGBC);
      
      vpGBC.gridx = vnGridX+1;
      vpGBC.anchor = GridBagConstraints.WEST;
      vpGBC.insets = (vpGBC.gridx == (mnInputColumns*2-1)) ? vpOuterInputInsets : vpInnerInputInsets;
      mpInputPanel.add(mpInputList.get(i), vpGBC);

      vpGBC.gridy += vpGBC.gridheight;
      if (((vnVisible+1) % vnRows) == 0)
      {
        vpGBC.gridy = mnHeaderRows;
        vnGridX += 2;
      }
      vnVisible++;
    }
  }

  /**
   * Enable/Disable the specified component <del>and the associated label</del>
   * @param ipComponent - The input to change
   * @param izEnabled - Enabled flag
   */
  protected void setInputEnabled(Component ipComponent, boolean izEnabled)
  {
    for (int i = 0; i < mpInputList.size(); i++)
    {
      Component j = mpInputList.get(i);
      if (j == ipComponent)
      {
        j.setEnabled(izEnabled);
//        j = mpLabelList.get(i);
//        j.setEnabled(izEnabled);
      }
    }
  }

  /**
   * Hide/Show the specified component and the associated label
   * @param ipComponent - The input to change
   * @param izVisible - Visibility flag
   */
  protected void setInputVisible(Component ipComponent, boolean izVisible)
  {
    for (int i = 0; i < mpInputList.size(); i++)
    {
      Component j = mpInputList.get(i);
      if (j == ipComponent)
      {
        j.setVisible(izVisible);
        j = mpLabelList.get(i);
        j.setVisible(izVisible);
      }
    }
    distributeInputsBetweenColumns();
    pack();
  }
  
  /**
   * Remove the specified component and its associated label
   * @param ipComponent
   */
  protected void removeInput(Component ipComponent)
  {
    for (int i = 0; i < mpInputList.size(); i++)
    {
      Component j = mpInputList.get(i);
      if (j == ipComponent)
      {
        mpInputPanel.remove(j);
        j = mpLabelList.get(i);
        mpInputPanel.remove(j);

        mpInputList.remove(i);
        mpLabelList.remove(i);
        break;
      }
    }
    distributeInputsBetweenColumns();
    pack();
  }
  

  /*========================================================================*/
  /*  ButtonPanel methods                                                   */
  /*========================================================================*/
  
  /**
   * Add the default buttons to the button panel
   */
  protected void addDefaultButtons()
  {
    /*
     * Define the default buttons
     */
    mpBtnSubmit = new SKDCButton("Submit", "Submit", 'S');
    mpBtnSubmit.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        okButtonPressed();
      }
    });
    
    mpBtnClear = new SKDCButton("Clear", "Clear", 'r');
    mpBtnClear.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        clearButtonPressed();
      }
    });

    mpBtnClose = new SKDCButton("Close", "Close", 'C');
    mpBtnClose.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeButtonPressed();
      }
    });

    /*
     * Add them to the panel
     */
    mpButtonPanel.add(mpBtnSubmit);
    mpButtonPanel.add(mpBtnClear);
    mpButtonPanel.add(mpBtnClose);
  }
  
  /**
   * Method for the Submit button.  Must be overridden by the child class.
   */
  @Override
  protected void okButtonPressed()
  {
    JOptionPane.showMessageDialog(this, "Not implemented");
    super.okButtonPressed();
  }
  
  /**
   * Method for the Clear button.  Must be overridden by the child class.
   */  
  @Override
  protected void clearButtonPressed()
  {
    JOptionPane.showMessageDialog(this, "Not implemented");
    super.clearButtonPressed();
  }
  
  /**
   * Method for the Close button.  Should be overridden by the child class.
   */  
  @Override
  protected void closeButtonPressed()
  {
    super.closeButtonPressed();
    close();
  }
  
  /**
   * Used to display warning messages on certain screens to reduce operator
   * error.
   * 
   * @param isWarning
   */
  protected void showWarning(String isWarning)
  {
    mzShowWarningPanel = true;
    mpWarningPanel.setVisible(mzShowWarningPanel);
    mpWarningLabel.setText("<HTML><B>WARNING:</B> " + isWarning + "</HTML>");
  }

  /*========================================================================*/
  /*  Extensions to allow allow addition of a list frame.  Yeah, I could    */
  /*  make another class for this.  In fact, I originally did, but then I   */
  /*  ran into some issues with Factory.create() and Java's lack of support */
  /*  for multiple inheritance, so here it is instead.              -Mike   */
  /*========================================================================*/
  protected DacTable mpTable = null;
  
  protected SKDCPopupMenu mpPopupMenu = null;
  
  protected SKDCButton mpBtnAddLine = null;
  protected SKDCButton mpBtnModLine = null;
  protected SKDCButton mpBtnDelLine = null;

  /**
   * Show the input table
   * 
   * @param isViewName for metadata
   */
  public void showTableWithDefaultButtons(String isViewName)
  {
    changeTable(isViewName);
    addLineButtons();
    pack();
  }

  public void showTable(String isViewName)
  {
    changeTable(isViewName);
    pack();
  }
  
  /**
   * Show the input table
   * 
   * @param isNewViewName metadata name.
   */
  public void changeTable(String isNewViewName)
  {
    mpTable = null;
    mpTable = new DacTable(new DacModel(new ArrayList<Map>(), isNewViewName));
    mpCenterPanel.add(mpTable.getScrollPane(), BorderLayout.CENTER);
    setTableMouseListener();
  }
  
  /**
   * Add the * Line buttons
   */
  private void addLineButtons()
  {
    /*
     * Define the default buttons
     */
    mpBtnAddLine = new SKDCButton("Add Line", "Add Line", 'A');
    mpBtnAddLine.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        addLineButtonPressed();
      }
    });
    
    mpBtnModLine = new SKDCButton("Modify Line", "Modify Line", 'M');
    mpBtnModLine.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        modifyLineButtonPressed();
      }
    });

    mpBtnDelLine = new SKDCButton("Delete Line", "Delete Line", 'D');
    mpBtnDelLine.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        deleteLineButtonPressed();
      }
    });
    
    mpButtonPanel.add(mpBtnAddLine);
    mpButtonPanel.add(mpBtnModLine);
    mpButtonPanel.add(mpBtnDelLine);
    mpButtonPanel.add(mpBtnClear);
    mpButtonPanel.add(mpBtnClose);
  }
  
  /**
   * Default mouse listener for the table 
   */
  protected void setTableMouseListener()
  {
    mpPopupMenu = new SKDCPopupMenu();
    mpTable.addMouseListener(new DacTableMouseListener(mpTable)
    {
      /**
       *  Defines popup menu items for <code>SKDCTable</code>, and adds listeners
       *  to them.
       */
      @Override
      public SKDCPopupMenu definePopup()
      {
        return(mpPopupMenu);
      }
      
      /**
       *  Display the Order Line screen.
       */
      @Override
      public void displayDetail()
      {
      }
    });
  }

  /**
   *  Method to refreshes display with List.
   *
   *  @param ipList List containing data for list.
   */
  protected void refreshTable(List ipList)
  {
    mpTable.refreshData(ipList);
  }

  /**
   * Do this when the frame opens
   */
  @Override
  public void internalFrameOpened(InternalFrameEvent e)
  {
    super.internalFrameOpened(e);
    
    /*
     * Unfortunately, I don't know a better way to limit the height of the 
     * scroll pane without manually changing the width, too.
     */
    if (mpTable != null)
    {
      Dimension d = mpTable.getScrollPane().getSize();
      if (d.getHeight() > 150)
        d.setSize(d.getWidth(), 150);
      mpTable.getScrollPane().setPreferredSize(d);
      pack();
    }
  }

  /**
   *  Add line
   */
  protected void addLineButtonPressed()
  {
    JOptionPane.showMessageDialog(this, "Not implemented");
  }

  /**
   * Modify Line
   */
  protected void modifyLineButtonPressed()
  {
    JOptionPane.showMessageDialog(this, "Not implemented");
  }

  /**
   * Delete Line
   */
  protected void deleteLineButtonPressed()
  {
    JOptionPane.showMessageDialog(this, "Not implemented");
  }
}
