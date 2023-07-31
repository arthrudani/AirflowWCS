package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistory;
import com.daifukuamerica.wrxj.dbadapter.data.TransactionHistoryData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction History Service
 *
 * @author mandrus
 */
public class TransactionHistoryService {
	private static final Logger logger = LoggerFactory.getLogger("TRANSACTIONHISTORY");
	private static final String metaId = "Transaction_All";
	protected AjaxResponse ajaxResponse;

	/**
	 * List all transaction history
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException {
		return listSearch(Factory.create(TransactionHistoryData.class));
	}

	/**
	 * List matching transaction history
	 *
	 * @param searchHeaderData
	 * @param searchLineData
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public TableDataModel listSearch(TransactionHistoryData historyKey) throws DBException, NoSuchFieldException {
		List<Map> tableData = Factory.create(TransactionHistory.class).getAllElements(historyKey);
		String[] dbColumns = AsrsMetaDataTransUtil.getInstance().getOrderedColumns(metaId, true);
		String[] transColumns = AsrsMetaDataTransUtil.getInstance().getTranslatedColumnHeaders(dbColumns, metaId);
		for(Map row : tableData)
		{
			row = AsrsMetaDataTransUtil.getInstance().databaseToUiTable(row, dbColumns, transColumns, metaId);
		}
		TableDataModel results = new TableDataModel(tableData);
		return results;
	}
}
