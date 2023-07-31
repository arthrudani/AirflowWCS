package com.daifukuamerica.wrxj.web.service;

import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;

public interface DataTableable
{
	/**
	 * Retrieve List<Map> using wrx data servers or data models of server-specific data 
	 * and process into web server TableDataModel for use in DataTables api. 
	 * 
	 * @return
	 * @throws DBException
	 * @throws NoSuchFieldException
	 */
	public TableDataModel list() throws DBException, NoSuchFieldException; 
}
