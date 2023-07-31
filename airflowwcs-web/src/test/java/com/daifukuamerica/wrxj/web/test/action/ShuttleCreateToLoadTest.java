package com.daifukuamerica.wrxj.web.test.action;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.web.test.LoggedInTest;
import com.daifukuamerica.wrxj.web.pages.Pages;


public class ShuttleCreateToLoadTest /*extends LoggedInTest*/
{
	
//	public String fromStationSelection = "0011"; 			// station selected in dropdown
//	public String fromToteScan = "756543"; 				// tote barcode that is scanned
//	public String fromSscidLookup = "373258078037500408"; 	//sscid that belongs to the scanned tote
//	public String fromOrderId = "100729"; 
//	public String item = "0001142815386025"; 
//	public String pickToLoadId = "373258078038012470"; 
//	public String pickQuantity = "4"; 
//	public String putWallLocationId = "01200103"; 
//
//	@Test
//	public void loadCreationTest()
//	{
//		Pages.shuttlePage().scanToteAtStation(fromToteScan, fromStationSelection);
//		assertTrue(Pages.shuttlePage().hasPickToLoadId()); 
//		assertTrue(Pages.shuttlePage().isWallLocationInputEnabled()); 
//		Pages.shuttlePage().scanPutWallLocation(putWallLocationId);
//	/*	IkeaLoadServer loadServer = Factory.create(IkeaLoadServer.class);
//		LoadData loadData = loadServer.getLoad(fromSscidLookup); 
//		assertTrue(loadData!=null && loadData.getAddress()!=null); 
//		assertTrue(loadData.getAddress().equals(Pages.shuttlePage().getPutWallLocation()));*/
//	}

}
