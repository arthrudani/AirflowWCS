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
package com.daifukuamerica.wrxj.web.service.dao;

import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.data.WrxLog;
import com.daifukuamerica.wrxj.dbadapter.data.WrxLogData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;

/**
 * Log View Service <br/>
 * Currently shows only error logs TODO: Use view that combines error and info
 *
 * @author mandrus
 */
public class LogViewService
{
	private static final String metaId = "WRxLog";
	protected AjaxResponse ajaxResponse;

	/**
	 * List all transaction history
	 *
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException
	{
		return listSearch(Factory.create(WrxLogData.class));
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
	public TableDataModel listSearch(WrxLogData historyKey) throws DBException, NoSuchFieldException
	{
		if (historyKey.getOrderByColumns().length == 0)
		{
			historyKey.addOrderByColumn(WrxLogData.DATE_TIME_NAME, true);
			historyKey.addOrderByColumn(WrxLogData.DESCRIPTION_NAME, true);
		}
		List<Map> tableData = Factory.create(WrxLog.class).getAllElements(historyKey);
		tableData = AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(tableData, metaId);
		TableDataModel results = new TableDataModel(tableData);
		return results;
	}
}
