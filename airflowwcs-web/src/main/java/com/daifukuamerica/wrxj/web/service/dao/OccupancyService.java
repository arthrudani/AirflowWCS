package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.OccupancyData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuoc.wrxj.custom.ebs.dbadapter.data.EBSTableJoin;

public class OccupancyService {
	private static final Logger logger = LoggerFactory.getLogger("OCCUPANCY");
	private final String metaId = "Occupancy";

	/**
	 * List all Occupancies
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException {
		return list(null);
	}

	/**
	 * List Occupancy by key
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TableDataModel list(OccupancyData ipLocKey) throws DBException, NoSuchFieldException {
		if (ipLocKey == null) {
			ipLocKey = Factory.create(OccupancyData.class);
		}
		final EBSTableJoin occupancyHandler = new EBSTableJoin();
		List<Map> occupancyData = (List<Map>) occupancyHandler.getOccupancyData(ipLocKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map m : occupancyData) {
			m = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(m, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(occupancyData);
		return results;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void executeSp() throws DBException, NoSuchFieldException {
		DBObject mpDBO = new DBObject();
		try {
			mpDBO.connect();

			TransactionToken tt = null;
			try {
				tt = mpDBO.startTransaction();
				mpDBO.executeStoreProcedure("SP_OCCUPANCY");
				 mpDBO.commitTransaction(tt);
			} finally {
				mpDBO.endTransaction(tt);
			}

		} finally {
			mpDBO.disconnect(false);
		}
	
	}

}
