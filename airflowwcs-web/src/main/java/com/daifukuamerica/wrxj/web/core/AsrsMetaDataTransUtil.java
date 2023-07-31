package com.daifukuamerica.wrxj.web.core;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBTrans;
import com.daifukuamerica.wrxj.web.core.connection.WrxjConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience Util for translating ASRS Metadata values to their human-readable values.
 *
 * Used mainly by TableService and TableController to build ajaxTables.
 *
 * Author: dystout
 * Created : May 31, 2017
 *
 */
public class AsrsMetaDataTransUtil
{
	/**
	* Log4j logger: AsrsMetaDataTransUtil
	*/
	private static final Logger logger = LoggerFactory.getLogger(AsrsMetaDataTransUtil.class);

	private static AsrsMetaData amd = Factory.create(AsrsMetaData.class);
	private static AsrsMetaDataTransUtil instance = null;

	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private static boolean translateDateTime = false;

	protected AsrsMetaDataTransUtil() throws DBException, NoSuchFieldException
	{
		if (WrxjConnection.dbObject == null || !WrxjConnection.dbObject.checkConnected())
		{
			WrxjConnection.getInstance().connect();
		}
	}


	public static AsrsMetaDataTransUtil getInstance()
	{
		if(instance == null){
			try
			{
				instance = new AsrsMetaDataTransUtil();
			}
			catch (DBException | NoSuchFieldException e)
			{
				logger.error("There was a problem reading ASRS Metadata: {}", e.getMessage());
			}
		}
		return instance;
	}

	@SuppressWarnings("rawtypes")
	public List<Map> getAsrsMetaData(String asrsMetaDataViewName)
	{
		AsrsMetaDataData mddata = Factory.create(AsrsMetaDataData.class);
		AsrsMetaData amd = Factory.create(AsrsMetaData.class);
		List<Map> metaData = null;

		try
		{
			mddata.clear();
			mddata.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, asrsMetaDataViewName);
			metaData = amd.getAllElements(mddata);
		}
		catch (DBException e)
		{
			logger.error("Unable to get Elements from AsrsMetaDataData: {}", e.getMessage());
		}

		return metaData;
	}

	/**
	 * Translate the given String array of column headers to the human-readable descriptions of
	 * those column names. The metaId is used to determine which table is being translated.
	 *
	 * @param dbColumns - string[] of column headers
	 * @param metaId - asrs metadata table id
	 * @return string[] of translated column headers
	 * @throws DBException
	 */
	public String[] getTranslatedColumnHeaders(String[] dbColumns, String metaId) throws DBException
	{
		AsrsMetaData amd = Factory.create(AsrsMetaData.class);
		String[] transColumns = dbColumns.clone();
		for(int i = 0; i<=dbColumns.length-1; i++)
		{
			String colTransName = amd.getFullName(metaId, dbColumns[i]);
		/*	colTransName = colTransName.replace(".", ""); // Periods are bad in our column headers because it formats the JSON weird*/
			transColumns[i] = colTransName;
		}
		return transColumns;
	}

	/**
	 * Take a table and format it for the JSON response to the datatables API, this includes replacing
	 * the header column names of the data with the translation value. The data in those columns is also translated
	 * iff the asrsmetadata has a flag of "Y" in the database. DBTrans and <b>custom DBTrans</b> instance will be
	 * used in order to produce the translated values
	 *
	 * @param maps - row
	 * @param utColumns - untranslated column names
	 * @param trColumns - translated column names
	 * @param metaId - asrs metadata id for the table
	 * @return row of translated data for a WRXJ table
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> translateColumnValueMap(List<Map> maps, String metaId) throws DBException, NoSuchFieldException
	{
		String[] dbColumns = getOrderedColumns(metaId, true);
		String[] transColumns = getTranslatedColumnHeaders(dbColumns, metaId);
		for (Map row : maps)
		{
			translateColumnValueMap(row, dbColumns, transColumns, metaId);
		}
		return maps;
	}

	/**
	 * Take a row in a table and format it for the JSON response to the datatables API, this includes replacing
	 * the header column names of the data with the translation value. The data in those columns is also translated
	 * iff the asrsmetadata has a flag of "Y" in the database. DBTrans and <b>custom DBTrans</b> instance will be
	 * used in order to produce the translated values
	 *
	 * @param map - row
	 * @param utColumns - untranslated column names
	 * @param trColumns - translated column names
	 * @param metaId - asrs metadata id for the table
	 * @return row of translated data for a WRXJ table
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map translateColumnValueMap(Map map, String[] utColumns, String[] trColumns, String metaId) throws NoSuchFieldException
	{
		List<Map> metaData = getAsrsMetaData(metaId);
		Map<String,Boolean> transMap = getAsrsTranslationMap(metaData); // determine which column data values require translation
		for(int i=0; i<=utColumns.length-1; i++)
		{
			if(map.containsKey(utColumns[i]))
			{
				Object objData = map.get(utColumns[i]); // get data
				if(transMap!=null&&transMap.size()>=utColumns.length){
					if(transMap.get(utColumns[i])){ // if the column value needs to be translated (checked from transMap)
						if(objData instanceof String)
						{
							objData = DBTrans.getStringValueNoExc(utColumns[i],(Integer)map.get(utColumns[i])); //translate data value
						}
						if(objData instanceof Integer)
						{
							objData = DBTrans.getStringValueNoExc(utColumns[i],(Integer)map.get(utColumns[i])); //translate data value
						}
					}
				}

				map.remove(utColumns[i]); //remove untranslated column name
				map.put(trColumns[i],objData); //put translated column name in with translated data value
			}
		}

		return map;
	}

	/**
	 * Take a row in a table and format it for the JSON response to the datatables API, this includes replacing
	 * the header column names of the data with the translation value. The data in those columns is also translated
	 * iff the asrsmetadata has a flag of "Y" in the database. DBTrans and <b>custom DBTrans</b> instance will be
	 * used in order to produce the translated values
	 *
	 * @param map - row
	 * @param utColumns - untranslated column names
	 * @param trColumns - translated column names
	 * @param metaId - asrs metadata id for the table
	 * @return row of translated data for a WRXJ table
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map databaseToUiTable(Map map, String[] utColumns, String[] trColumns, String metaId)
	{
		List<Map> metaData = getAsrsMetaData(metaId);
		Map<String,Boolean> transMap = getAsrsTranslationMap(metaData); // determine which column data values require translation
		for (int i = 0; i <= utColumns.length - 1; i++)
		{
			if (map.containsKey(utColumns[i]))
			{
				Object objData = map.remove(utColumns[i]);

				// If the column name is not the same as the displayed column name,
				// this should probably be displayed
				if (!utColumns[i].equals(trColumns[i]))
				{
					// Translate this column if necessary
					Boolean needsTranslation = transMap.get(utColumns[i]);
					if (needsTranslation != null && needsTranslation)
					{
						try
						{
							if (objData instanceof String)
							{
								objData = DBTrans.getStringValue(utColumns[i], Integer.parseInt((String) objData));
							}
							else if (objData instanceof Integer)
							{
								objData = DBTrans.getStringValue(utColumns[i], (Integer) objData);
							}
						}
						catch (NoSuchFieldException nse)
						{
							if (Integer.parseInt(objData.toString()) == 0)
							{
								// This is most likely an empty value
								objData = "";
							}
							else
							{
								objData = String.format("Unknown (%s)", objData.toString());
							}
						}
					}
					else if (translateDateTime && objData instanceof Timestamp)
					{
						try
						{
							objData = timeFormatter.format(new Date(((Date)objData).getTime()).toInstant().atZone(ZoneId.systemDefault()));
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					// put translated column name in with translated data value
					map.put(trColumns[i],objData);
				}
			}
		}

		return map;
	}

	@SuppressWarnings("rawtypes")
	public Map<String,Boolean> getAsrsTranslationMap(List<Map> metadata)
	{
		Map<String,Boolean> translationMap = new HashMap<String, Boolean>();
		for(Map meta : metadata)
		{
			String sIsTranslation = (String) meta.get("SISTRANSLATION");
			String sColumnName = (String) meta.get("SCOLUMNNAME");
			translationMap.put(sColumnName, sIsTranslation.equalsIgnoreCase("Y"));
		}
		return translationMap;
	}

	public String[] getTranslatedColumnNames(String asrsMetaDataViewName)
	{

		AsrsMetaData amd = Factory.create(AsrsMetaData.class);
		String[] translatedColumns = null;
		try
		{
			String[] untranslatedColumns = amd.getOrderedColumns(asrsMetaDataViewName);
			translatedColumns = untranslatedColumns.clone();
			for(int i=0; i<=untranslatedColumns.length-1;i++)
			{
				translatedColumns[i] = amd.getFullName(asrsMetaDataViewName, untranslatedColumns[i]);
			}

		} catch (DBException e)
		{
			logger.error("Unable to get elements from AsrsMetaData: {}", e.getMessage());
		}
		return translatedColumns;
	}


	public String[] getOrderedColumns(String metaId, boolean b)
	{

		try
		{
			return amd.getOrderedColumns(metaId, true);
		} catch (DBException e)
		{
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}


}
