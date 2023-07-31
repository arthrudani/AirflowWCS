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

import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dbadapter.data.Device;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import com.daifukuamerica.wrxj.web.core.AsrsMetaDataTransUtil;
import com.daifukuamerica.wrxj.web.core.StackTraceFilter;
import com.daifukuamerica.wrxj.web.model.json.AjaxResponse;
import com.daifukuamerica.wrxj.web.model.json.TableDataModel;
import com.daifukuamerica.wrxj.web.model.json.wrx.DeviceDataModel;
import com.daifukuamerica.wrxj.web.service.DataTableable;
import com.daifukuamerica.wrxj.web.ui.AjaxResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Device Service
 */
public class DeviceService implements DataTableable
{
	private static final Logger logger = LoggerFactory.getLogger("Device");

	private final String metaId = "Device";
	
    /**
     * Find a specific device by id
     *
     * @param deviceId
     * @return DeviceDataModel
     */
    public DeviceDataModel find(String deviceId)
    {
        DeviceDataModel dm = null;
        try
        {
            Device handler = Factory.create(Device.class);
            DeviceData key = Factory.create(DeviceData.class);
            key.setKey(DeviceData.DEVICEID_NAME, deviceId);
            dm = new DeviceDataModel(handler.getElement(key, DBConstants.NOWRITELOCK));
        }
        catch (Exception e)
        {
            logger.error("DeviceService (find) Exception for device=[{}]", deviceId, StackTraceFilter.filter(e));
        }
        return dm;
    }

    /**
     * List all devices
     */
    public TableDataModel list() throws DBException, NoSuchFieldException
    {
      return list(null);
    }
    
    /**
     * List devices with key
     */
	@SuppressWarnings("rawtypes")
    public TableDataModel list(DeviceData ipKey) throws DBException, NoSuchFieldException
	{
    	if (ipKey == null)
    	{
    	    ipKey = Factory.create(DeviceData.class);
    	}
        Device handler = Factory.create(Device.class);
        List<Map> listData = handler.getAllElements(ipKey);
        
        AsrsMetaDataTransUtil.getInstance().translateColumnValueMap(listData, metaId);
        return new TableDataModel(listData);
	}
	
	/**
     * Modify a shelf position with DeviceDataModel
     *
     * @param dataModel - DeviceDataModel
     * @return {@link AjaxResponse}
     */
    public AjaxResponse modify(DeviceDataModel dataModel)
    {
        AjaxResponse ajaxResponse = new AjaxResponse();
        try
        {
            if (SKDCUtility.isBlank(dataModel.getDeviceId()))
            {
                throw new IllegalArgumentException("Device ID may not be blank");
            }

            // All columns must be populated for modifyDevice
            Device handler = Factory.create(Device.class);
            DeviceData key = Factory.create(DeviceData.class);
            key.setKey(DeviceData.DEVICEID_NAME, dataModel.getDeviceId());
            DeviceData vpDeviceData = handler.getElement(key, DBConstants.NOWRITELOCK);
            // Do update
            vpDeviceData.setKey(DeviceData.DEVICEID_NAME, dataModel.getDeviceId());
            vpDeviceData.setOperationalStatus(dataModel.getOperationalStatus());
            Factory.create(StandardDeviceServer.class).modifyDevice(vpDeviceData);
        }
        catch (IllegalArgumentException e)
        {
            ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, e.getMessage());
            logger.error("DeviceService (modify) Exception for ({})", dataModel.getDeviceId(), StackTraceFilter.filter(e));
        }
        catch (DBException e)
        {
            ajaxResponse.setResponse(AjaxResponseCodes.FAILURE, "There was a database exception: " + e.getMessage());
            logger.error("DeviceService (modify) Exception for ({})", dataModel.getDeviceId(), StackTraceFilter.filter(e));
        }
        if (ajaxResponse.getResponseCode() == AjaxResponseCodes.DEFAULT)
            ajaxResponse.setResponse(AjaxResponseCodes.SUCCESS, "Updated device " + dataModel.getDeviceId());
        return ajaxResponse;
    }
}
