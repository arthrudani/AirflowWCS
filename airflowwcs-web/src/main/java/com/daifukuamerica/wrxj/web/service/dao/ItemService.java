/* ***************************************************************************
  Copyright (c) 2019 Daifuku North America Holding Company. All Rights Reserved.

  THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.
  NO PART OF THIS WORK MAY BE USED, PRACTICED, PERFORMED COPIED, DISTRIBUTED,
  REVISED, MODIFIED, TRANSLATED, ABRIDGED, CONDENSED, EXPANDED, COLLECTED,
  COMPILED, LINKED, RECAST, TRANSFORMED OR ADAPTED WITHOUT THE PRIOR WRITTEN
  CONSENT OF Daifuku America Corporation. ANY USE OR EXPLOITATION OF THIS
  WORK WITHOUT AUTHORIZATION COULD SUBJECT THE PERPETRATOR TO CRIMINAL AND
  CIVIL LIABILITY.
 ****************************************************************************/
package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.service.DataTableable;

/**
 * Service class for item masters
 */
public class ItemService implements DataTableable
{
	public final String metaId = "ItemMaster";

	@Override
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ItemMasterData getItemMasterData(String itemName) throws DBException
	{
		StandardInventoryServer invServer = new StandardInventoryServer();
		return invServer.getItemMasterData(itemName);
	}

	public String getItemMasterDescription(String itemName) throws DBException
	{
		StandardInventoryServer invServer = new StandardInventoryServer();
		return invServer.getItemMasterDescription(itemName);
	}

	/**
	 * List item masters by search criteria
	 *
	 * @param ipSearchKey
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(ItemMasterData ipSearchKey) throws DBException, NoSuchFieldException
	{
		ItemMaster vpHandler = Factory.create(ItemMaster.class);
		List<Map> vpData = vpHandler.getAllElements(ipSearchKey);
		vpData = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(vpData, metaId);
		return new TableDataModel(vpData);
	}
}
