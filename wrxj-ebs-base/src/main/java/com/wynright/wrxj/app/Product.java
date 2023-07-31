/* ***************************************************************************
  $Workfile$
  $Revision$
  $Date$

  Copyright (c) 2017 Daifuku North America Holding Company. All Rights Reserved.
  
  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES. 
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED, 
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED, 
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN 
  CONSENT OF Wynright Corporation.  ANY USE OR EXPLOITATION OF THIS WORK 
  WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND CIVIL 
  LIABILITY.
 ****************************************************************************/
package com.wynright.wrxj.app;

/**
 * AES_SYS_PRODUCTS contains an enum listing.  These are hard-coded here.
 * 
 * @author mandrus
 */
public enum Product {
  CUSTOM(0, "Custom"),
  CME(1, "Communicator"),
  PPE(2, "OF Engines"),
  LLE(3, "Labeler PLC"),
  CLE(4, "Convey PLC"),
  LCE(5, "Labeler PC"),
  CCE(6, "Convey PC"),
  TST(7, "Test Module"),
  VIE_WEB(11, "Visibility Web"),
  VIE_WPF(12, "Visibility Wpf"),
  AUE(13, "Auditor"),
  DEE(14, "DEE 4 test"),
  TRE(15, "Transporter"),
  INV(16, "Inventory"),
  VLE(17, "Voice Link"),
  PDE(18, "PalDepal"),
  VIG(19, "Visibility Graphics"),
  RCE(20, "Receiving"),
  LME(21, "Lane Management"),
  CID(22, "Convey Inventory Database"),
  WME(23, "Wave Management Engine"),
  EMT(24, "Engine Model Task Based"),
  EMM(25, "Engine Model Machine Based"),
  EMU(26, "Engine Model User Based"),
  TLE(27, "Track Loader Engine"),
  WRX(28, "Warehouse Rx"),
  OMS(29, "Order Management System"),
  AES(99, "AE System"),
  ASM(998, "ASM"),
  WGE(999, "Wynsoft Global");
  
  int mnId;
  String msDescription;
  private Product(int id, String description)
  {
    mnId = id;
    msDescription = description;
  }
  
  public int getId()
  {
    return mnId;
  }
  
  public String getDescription()
  {
    return msDescription;
  }
  
  public String describe()
  {
    return toString() + " (ID=" + mnId + ", DESCRIPTION=" + msDescription + ")";
  }
}
