package com.daifukuamerica.wrxj.swingui.item;

import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.swing.SKDCDoubleField;
import com.daifukuamerica.wrxj.swing.SKDCIntegerField;
import com.daifukuamerica.wrxj.swing.SKDCLabel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class ItemSizingPanel extends JPanel
{
  private SizingPanel mpPieceSizePanel = new SizingPanel("Piece", false);
//  private SizingPanel mpIPackSizePanel = new SizingPanel("Inner Pack", true);
  private SizingPanel mpCaseSizePanel  = new SizingPanel("Case", true);

  /*
   * Constructors
   */
  public ItemSizingPanel()
  {
    super();
    addPanels();
  }

  /*
   * Add the sizing panels
   */
  private void addPanels()
  {
    add(mpPieceSizePanel);
//    add(mpIPackSizePanel);
    add(mpCaseSizePanel);
  }
  
  /**
   * Set the panel (and contents) to enabled or disabled
   */
  @Override
  public void setEnabled(boolean izEnabled)
  {
    super.setEnabled(izEnabled);
    mpPieceSizePanel.setEnabled(izEnabled);
//    mpIPackSizePanel.setEnabled(izEnabled);
    mpCaseSizePanel.setEnabled(izEnabled);
  }
  
  /**
   * <B>Description:</B> Internal class for sizing info<BR>
   *
   * @author       mandrus<BR>
   * @version      1.0
   * 
   * <BR>Copyright (c) 2005 by Daifuku America Corporation
   */
  protected class SizingPanel extends JPanel
  {
    SKDCIntegerField mpCountField  = new SKDCIntegerField(1,7);
    SKDCDoubleField  mpWeightField = new SKDCDoubleField(7);
    SKDCDoubleField  mpLengthField = new SKDCDoubleField(7);
    SKDCDoubleField  mpHeightField = new SKDCDoubleField(7);
    SKDCDoubleField  mpWidthField  = new SKDCDoubleField(7);
    
    boolean mzChangeCount = true;

    private SizingPanel(String isTitle, boolean izChangeCount)
    {
      super();
      
      mzChangeCount = izChangeCount;
      
      setLayout(new GridBagLayout());
      setBorder(new TitledBorder(new EtchedBorder(), isTitle));

      GridBagConstraints ipGBC = new GridBagConstraints();
      ipGBC.gridx = 0;
      ipGBC.gridy = GridBagConstraints.RELATIVE;
      ipGBC.gridwidth = 1;
      ipGBC.anchor = GridBagConstraints.EAST;
      ipGBC.weightx = 0.2;
      ipGBC.weighty = 0.8;
      ipGBC.insets = new Insets(4, 20, 4, 2);

      add(new SKDCLabel("Pieces:"), ipGBC);
      add(new SKDCLabel("Weight:"), ipGBC);
      add(new SKDCLabel("Length:"), ipGBC);
      add(new SKDCLabel("Height:"), ipGBC);
      add(new SKDCLabel("Width:"), ipGBC);
      
      ipGBC.gridx = 1;
      ipGBC.anchor = GridBagConstraints.WEST;
      ipGBC.insets = new Insets(4, 2, 4, 20);

      add(mpCountField, ipGBC);
      mpCountField.setEnabled(izChangeCount);
      add(mpWeightField, ipGBC);
      add(mpLengthField, ipGBC);
      add(mpHeightField, ipGBC);
      add(mpWidthField, ipGBC);
    }
    
    /*
     * Getters
     */
    private int    getSizingCount()  {    return mpCountField.getValue();    }
    private double getSizingWeight() {    return mpWeightField.getValue();   }
    private double getSizingLength() {    return mpLengthField.getValue();   }
    private double getSizingWidth()  {    return mpWidthField.getValue();    }
    private double getSizingHeight() {    return mpHeightField.getValue();   }
    
    /*
     * Setters
     */
    private void setSizingCount(int inValue)     { mpCountField.setValue(inValue);  }
    private void setSizingWeight(double idValue) { mpWeightField.setValue(idValue); }
    private void setSizingLength(double idValue) { mpLengthField.setValue(idValue); }
    private void setSizingWidth(double idValue)  { mpWidthField.setValue(idValue);  }
    private void setSizingHeight(double idValue) { mpHeightField.setValue(idValue); }
    
    /**
     * Set the panel (and contents) to enabled or disabled
     */
    @Override
    public void setEnabled(boolean izEnabled)
    {
      super.setEnabled(izEnabled);
      if (mzChangeCount)
      {
        mpCountField.setEnabled(izEnabled);
      }
      mpWeightField.setEnabled(izEnabled);
      mpLengthField.setEnabled(izEnabled);
      mpWidthField.setEnabled(izEnabled);
      mpHeightField.setEnabled(izEnabled);
    }
  }
  
  /*
   * Public Getters
   */
  public double getPieceWeight() { return mpPieceSizePanel.getSizingWeight(); }
  public double getPieceLength() { return mpPieceSizePanel.getSizingLength(); }
  public double getPieceWidth()  { return mpPieceSizePanel.getSizingWidth();  }
  public double getPieceHeight() { return mpPieceSizePanel.getSizingHeight(); }

  public int    getCaseCount()   { return mpCaseSizePanel.getSizingCount();  }
  public double getCaseWeight()  { return mpCaseSizePanel.getSizingWeight(); }
  public double getCaseLength()  { return mpCaseSizePanel.getSizingLength(); }
  public double getCaseWidth()   { return mpCaseSizePanel.getSizingWidth();  }
  public double getCaseHeight()  { return mpCaseSizePanel.getSizingHeight(); }
  
  public void getSizingInfo(ItemMasterData ipIMData)
  {
    ipIMData.setWeight(getPieceWeight());
    ipIMData.setItemLength(getPieceLength());
    ipIMData.setItemHeight(getPieceHeight());
    ipIMData.setItemWidth(getPieceWidth());

    ipIMData.setPiecesPerUnit(getCaseCount());
    ipIMData.setCaseWeight(getCaseWeight());
    ipIMData.setCaseLength(getCaseLength());
    ipIMData.setCaseHeight(getCaseHeight());
    ipIMData.setCaseWidth(getCaseWidth());
  }
  
  /*
   * Public Setters
   */
  public void setPieceWeight(double idValue) { mpPieceSizePanel.setSizingWeight(idValue); }
  public void setPieceLength(double idValue) { mpPieceSizePanel.setSizingLength(idValue); }
  public void setPieceWidth(double idValue)  { mpPieceSizePanel.setSizingWidth(idValue);  }
  public void setPieceHeight(double idValue) { mpPieceSizePanel.setSizingHeight(idValue); }

  public void setCaseCount(int inValue)      { mpCaseSizePanel.setSizingCount(inValue);  }
  public void setCaseWeight(double idValue)  { mpCaseSizePanel.setSizingWeight(idValue); }
  public void setCaseLength(double idValue)  { mpCaseSizePanel.setSizingLength(idValue); }
  public void setCaseWidth(double idValue)   { mpCaseSizePanel.setSizingWidth(idValue);  }
  public void setCaseHeight(double idValue)  { mpCaseSizePanel.setSizingHeight(idValue); }

  public void setSizingInfo(ItemMasterData ipIMData)
  {
    setPieceWeight(ipIMData.getWeight());
    setPieceLength(ipIMData.getItemLength());
    setPieceHeight(ipIMData.getItemHeight());
    setPieceWidth(ipIMData.getItemWidth());

    setCaseCount(ipIMData.getPiecesPerUnit());
    setCaseWeight(ipIMData.getCaseWeight());
    setCaseLength(ipIMData.getCaseLength());
    setCaseHeight(ipIMData.getCaseHeight());
    setCaseWidth(ipIMData.getCaseWidth());
  }
}
