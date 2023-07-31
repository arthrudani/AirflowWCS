package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Warehouse;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;

public class EquipmentsService {
	private static final Logger logger = LoggerFactory.getLogger("Location");

	private final String metaId = "Location";
	
	/**
	 * List all equipments
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		return list(null);
	}

	/**
	 * List equipments by key
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TableDataModel list(LocationData ipLocKey) throws DBException, NoSuchFieldException
	{
		if (ipLocKey == null)
		{
			ipLocKey = Factory.create(LocationData.class);
		}
		Warehouse whs = Factory.create(Warehouse.class);
        final EBSTableJoin vpTJLoadHandler = new EBSTableJoin();
		List<Map> utEquipmentData = (List<Map>)vpTJLoadHandler.getEquipmentsData(ipLocKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map m : utEquipmentData)
		{
			String w = (String)m.get(LocationData.WAREHOUSE_NAME);
			String a = (String)m.get(LocationData.ADDRESS_NAME);
			String p = (String)m.get(LocationData.SHELFPOSITION_NAME);
			m = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(m, dbColumns, transColumns, metaId);

			m.put("id", w + "-" + a + "-" + p);
			m.put("warehouseType", whs.getWarehouseType(w));
		}
		TableDataModel results = new TableDataModel(utEquipmentData);
		return results;
	}
	

}
