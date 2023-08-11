package com.daifukuoc.wrxj.custom.ebs.dbadapter.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.daifukuamerica.wrxj.dbadapter.DBHelper;
import com.daifukuamerica.wrxj.dbadapter.data.AlertData;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.MoveCommandData;
import com.daifukuamerica.wrxj.dbadapter.data.OccupancyData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.PurchaseOrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.TableJoin;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.jdbc.EBSConstants;

public class EBSTableJoin extends TableJoin {
    public EBSTableJoin() {
        super();
    }

    public List<Map> getLoadLineItemsForThisItem(String isBagId, String isLot, String LoadId) throws DBException {

        StringBuffer tmpString = new StringBuffer();
        tmpString.append("SELECT lt.* FROM ").append("Load ld inner join LoadLineItem lt  ")
                .append("  on ld.sLoadID = lt.sLoadID ")
                .append(" AND lt.sLineId = '").append(isBagId).append("' ")
                .append(" AND lt.sLot = '").append(isLot).append("'");
        if (LoadId != null && !LoadId.isEmpty()) {
            tmpString.append(" AND ld.sLoadID = '").append(LoadId).append("'");
        }

        StringBuilder vpSql = new StringBuilder(tmpString);
        List<Map> idList = fetchRecords(vpSql.toString());
        return (idList);
    }

    public List<Map> getLoadLineItemsForThisLoad(String sLoadId) throws DBException {

        StringBuffer tmpString = new StringBuffer();
        tmpString.append("SELECT lt.* FROM ").append("Load ld inner join LoadLineItem lt  ")
                .append("  on ld.sLoadID = lt.sLoadID ")
                .append(" AND ld.sLoadId = '").append(sLoadId).append("' ");

        StringBuilder vpSql = new StringBuilder(tmpString);
        List<Map> idList = fetchRecords(vpSql.toString());
        return (idList);
    }

    /**
     * Method to return a the input station for an aisle
     *
     * @throws DBException for DB access errors.
     */
    public String getInputStationByDevice(String isDeviceID) throws DBException {
        StringBuilder vpSql = new StringBuilder("SELECT sStationName FROM STATION  ")
                .append(" WHERE iStationType = ").append(DBConstants.INPUT).append(" ")
                .append(" AND sDeviceID = '").append(isDeviceID).append("' ");

        String vsInputStn = "";
        List<Map> vpList = fetchRecords(vpSql.toString());
        if (!vpList.isEmpty()) {
            vsInputStn = DBHelper.getStringField(vpList.get(0), StationData.STATIONNAME_NAME);
        }

        return vsInputStn;
    }

    /**
     * Method to return a the input station for an aisle
     *
     * @throws DBException for DB access errors.
     */
    public String getOutputStationByDevice(String isDeviceID) throws DBException {
        StringBuilder vpSql = new StringBuilder("SELECT sStationName FROM STATION  ")
                .append(" WHERE iStationType = ").append(DBConstants.OUTPUT).append(" ")
                .append(" AND sDeviceID = '").append(isDeviceID).append("' ");

        String vsInputStn = "";
        List<Map> vpList = fetchRecords(vpSql.toString());
        if (!vpList.isEmpty()) {
            vsInputStn = DBHelper.getStringField(vpList.get(0), StationData.STATIONNAME_NAME);
        }

        return vsInputStn;
    }

    /**
     * Method to return a the input station for an aisle
     *
     * @throws DBException for DB access errors.
     */
    public String getItemOnLoad(String isLoadID) throws DBException {
        StringBuilder vpSql = new StringBuilder("SELECT sItem FROM LOADLINEITEM  ")
                .append(" WHERE sLoadID = '").append(isLoadID).append("' ");

        String vsItem = "";
        List<Map> vpList = fetchRecords(vpSql.toString());
        if (!vpList.isEmpty()) {
            vsItem = DBHelper.getStringField(vpList.get(0), LoadLineItemData.ITEM_NAME);
        }

        return vsItem;
    }

    /**
     * Find the oldest item details that are available, from locations that are available. The oldest item detail could
     * be by Aging Date (defaults to store date) or by Expiration Date.
     *
     * @param isItem <code>String</code> containing item.
     * @param isLot <code>String</code> containing lot.
     * @param izSearchByZeroAllocated <code>boolean</code> if <code>true</code> means the search should be by loads that
     *        have no allocations against them.
     *
     * @param ipCustomObj var. arg. to allow for custom objects to be passed in for project customisations.
     * @return LoadLineItemData object containing Item Detail info. matching our search criteria.
     * @throws DBException
     */
    public List<Map> getOldestItemDetails(String isItem, String isLot, boolean izSearchByZeroAllocated,
            Object... ipCustomObj) throws DBException {
        // Figure out what how to deal with the
        // lot number.
        String lotString = null;
        String quantitySearch, orderByClause;

        // MCM, EBS 21Oct2020
        // Bagid = sItem is unique
        // no longer include the flight # in the query
        // if a bag gets rejected it can get restored with bagid, but without flight#
        // if we include slot = flight# is query for retrieval order it won't come out.
//	     if (isLot.trim().length() > 0)
//	     {
//	       lotString = "sLot = '" + isLot + "' ";
//	     }

        if (izSearchByZeroAllocated)
            quantitySearch = "id.fAllocatedQuantity = 0 ";
        else
            quantitySearch = "(id.fCurrentQuantity - id.fAllocatedQuantity) > 0 ";

        if (Factory.create(ItemMaster.class).isExpirationRequired(isItem))
            orderByClause = "ORDER BY id.iPriorityAllocation, id.dExpirationDate, id.dAgingDate";
        else
            orderByClause = "ORDER BY id.iPriorityAllocation, id.dAgingDate";

        StringBuffer tmpString = new StringBuffer();

        // MCM, EBS
        // look for any bag within flight # (lot)
        if (isItem.equals(EBSConstants.ANY_BAG_IN_FN)) {
            if (isLot.trim().length() <= 0) {
                return (null);
            }
            tmpString.append("SELECT id.* FROM ").append("Load ld, LoadLineItem id, Location lc, ItemMaster im, ")
                    .append("Device dv WHERE ld.sLoadID = id.sLoadID AND ").append("ld.sWarehouse = lc.sWarehouse AND ")
                    .append("ld.sAddress = lc.sAddress AND ").append("im.sItem = id.sItem AND ")
                    .append("dv.sDeviceID = lc.sDeviceID AND ").append("im.iHoldType = ").append(DBConstants.ITMAVAIL)
                    .append(" AND ").append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
                    .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL);
        } else {
            tmpString.append("SELECT id.* FROM ").append("Load ld, LoadLineItem id, Location lc, ItemMaster im, ")
                    .append("Device dv WHERE ld.sLoadID = id.sLoadID AND ").append("ld.sWarehouse = lc.sWarehouse AND ")
                    .append("ld.sAddress = lc.sAddress AND ").append("im.sItem = id.sItem AND ")
                    .append("dv.sDeviceID = lc.sDeviceID AND ").append("im.iHoldType = ").append(DBConstants.ITMAVAIL)
                    .append(" AND ").append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
                    .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ").append("id.sItem = '")
                    .append(isItem).append("' ");
        }

        StringBuilder vpSql = new StringBuilder(tmpString).append("AND ");
        if (lotString != null)
            vpSql.append(lotString).append(" AND ");

        vpSql.append(quantitySearch).append(orderByClause);

        List<Map> idList = fetchRecords(vpSql.toString());
        if ((mzAllowAltLots) && lotString != null && (idList == null || idList.size() == 0)) { // Try again with no Lot
                                                                                               // specified.
            vpSql.setLength(0);
            vpSql.append(tmpString).append("AND ").append(quantitySearch).append(orderByClause);
            idList = fetchRecords(vpSql.toString());
        }

        String lotSQL = (lotString == null) ? "" : "AND " + lotString;
        if (allocDiag != null && idList.isEmpty()) {
            String sql = "SELECT id.* FROM Load ld, LoadLineItem id, Location lc, "
                    + "ItemMaster im, Device dv WHERE ld.sLoadID = id.sLoadID AND "
                    + "ld.sWarehouse = lc.sWarehouse AND ld.sAddress = lc.sAddress AND "
                    + "im.sItem = id.sItem AND dv.sDeviceID = lc.sDeviceID AND " + "id.sItem = \'" + isItem + "\' ";
            List<Map> tryList1 = fetchRecords(sql + lotSQL);
            if (tryList1.isEmpty()) {
                if (!mzAllowAltLots) {
                    allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                            "Allocation likely failed for one the following reasons:"
                                    + "(1) The location device(s) do not match the " + "aisle controlling device. "
                                    + "(2) There are no item details for item " + isItem + " and lot " + isLot + "\n"
                                    + "NOTE: Alternate lot search is not enabled.");
                } else {
                    tryList1 = fetchRecords(sql);
                    if (tryList1.isEmpty()) {
                        allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                "Allocation likely failed for one the following reasons:"
                                        + "(1) The location device(s) do not match the " + "aisle controlling device. "
                                        + "(2) There are no item details for item " + isItem
                                        + "NOTE: Alternate lot search is not enabled.");
                    }
                }
            }

            if (!tryList1.isEmpty()) {
                sql = sql + lotSQL;
                sql = sql + "AND im.iHoldType = " + DBConstants.ITMAVAIL + " AND " + "id.iHoldType = "
                        + DBConstants.ITMAVAIL + " AND " + "lc.iLocationStatus = " + DBConstants.LCAVAIL + " ";
                List<Map> tryList2 = fetchRecords(sql);
                if (tryList2.isEmpty()) {
                    allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                            "Allocation failed because one or more of the "
                                    + "following must be marked available: Location,"
                                    + "ItemMaster, or Item Detail for item " + isItem);
                } else {
                    sql = sql + "AND ld.iLoadMoveStatus IN (" + DBConstants.NOMOVE + ", " + DBConstants.RETRIEVEPENDING
                            + ", " + DBConstants.RETRIEVING + ") ";
                    List<Map> tryList3 = fetchRecords(sql);
                    if (tryList3.isEmpty()) {
                        allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                "Allocation failed because load with oldest " + "item detail for item " + isItem
                                        + " must have one of the following status\': " + "(1) No Move "
                                        + "(2) Retrieve Pending " + "(3) Retrieving");
                    } else {
                        sql = sql + "AND dv.iOperationalStatus != " + DBConstants.INOP + " ";
                        List<Map> tryList4 = fetchRecords(sql);
                        if (tryList4.isEmpty()) {
                            allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                    "Allocation failed because devices " + "of aisles with item " + isItem + " are not "
                                            + "operational.");
                        }
                    }
                }
            }
        }

        return (idList);
    }

    /**
     * Find the oldest item details that are available, from locations that are available. The oldest item detail could
     * be by Aging Date (defaults to store date) or by Expiration Date.
     *
     * @param isItem <code>String</code> containing item.
     * @param isLot <code>String</code> containing lot.
     * @param izSearchByZeroAllocated <code>boolean</code> if <code>true</code> means the search should be by loads that
     *        have no allocations against them.
     *
     * @param ipCustomObj var. arg. to allow for custom objects to be passed in for project customisations.
     * @return LoadLineItemData object containing Item Detail info. matching our search criteria.
     * @throws DBException
     */
    public List<Map> getOldestItemDetailsByAisle(String isDeviceID, String isItem, String isLot,
            boolean izSearchByZeroAllocated, Object... ipCustomObj) throws DBException {
        // Figure out what how to deal with the
        // lot number.
        String lotString = null;
        String quantitySearch, orderByClause;
        String deviceIDSearch = null;

        if (isLot.trim().length() > 0) {
            lotString = "sLot = '" + isLot + "' ";
        }

        if (isDeviceID.trim().length() > 0) {
            deviceIDSearch = "ld.sDeviceID = '" + isDeviceID + "' AND ";
        }

        if (izSearchByZeroAllocated)
            quantitySearch = "id.fAllocatedQuantity = 0 ";
        else
            quantitySearch = "(id.fCurrentQuantity - id.fAllocatedQuantity) > 0 ";

        if (Factory.create(ItemMaster.class).isExpirationRequired(isItem))
            orderByClause = "ORDER BY id.iPriorityAllocation, id.dExpirationDate, id.dAgingDate";
        else
            orderByClause = "ORDER BY id.iPriorityAllocation, id.dAgingDate";

        StringBuffer tmpString = new StringBuffer();

        // MCM, EBS
        // look for any bag within flight # (lot)
        if (isItem.equals(EBSConstants.ANY_BAG_IN_FN)) {
            if (isLot.trim().length() <= 0) {
                return (null);
            }
            tmpString.append("SELECT id.* FROM ").append("Load ld, LoadLineItem id, Location lc, ItemMaster im, ")
                    .append("Device dv WHERE ld.sLoadID = id.sLoadID AND ").append("ld.sWarehouse = lc.sWarehouse AND ")
                    .append("ld.sAddress = lc.sAddress AND ").append("im.sItem = id.sItem AND ")
                    .append("dv.sDeviceID = lc.sDeviceID AND ").append("im.iHoldType = ").append(DBConstants.ITMAVAIL)
                    .append(" AND ").append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
                    .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL);
        } else {
            tmpString.append("SELECT id.* FROM ").append("Load ld, LoadLineItem id, Location lc, ItemMaster im, ")
                    .append("Device dv WHERE ld.sLoadID = id.sLoadID AND ").append("ld.sWarehouse = lc.sWarehouse AND ")
                    .append("ld.sAddress = lc.sAddress AND ").append("im.sItem = id.sItem AND ")
                    .append("dv.sDeviceID = lc.sDeviceID AND ").append("im.iHoldType = ").append(DBConstants.ITMAVAIL)
                    .append(" AND ").append("id.iHoldType = ").append(DBConstants.ITMAVAIL).append(" AND ")
                    .append("lc.iLocationStatus = ").append(DBConstants.LCAVAIL).append(" AND ").append("id.sItem = '")
                    .append(isItem).append("' ");
        }

        StringBuilder vpSql = new StringBuilder(tmpString).append("AND ");
        if (lotString != null)
            vpSql.append(lotString).append(" AND ");

        vpSql.append(deviceIDSearch).append(quantitySearch).append(orderByClause);

        List<Map> idList = fetchRecords(vpSql.toString());
        if ((mzAllowAltLots) && lotString != null && (idList == null || idList.size() == 0)) { // Try again with no Lot
                                                                                               // specified.
            vpSql.setLength(0);
            vpSql.append(tmpString).append("AND ").append(quantitySearch).append(orderByClause);
            idList = fetchRecords(vpSql.toString());
        }

        String lotSQL = (lotString == null) ? "" : "AND " + lotString;
        if (allocDiag != null && idList.isEmpty()) {
            String sql = "SELECT id.* FROM Load ld, LoadLineItem id, Location lc, "
                    + "ItemMaster im, Device dv WHERE ld.sLoadID = id.sLoadID AND "
                    + "ld.sWarehouse = lc.sWarehouse AND ld.sAddress = lc.sAddress AND "
                    + "im.sItem = id.sItem AND dv.sDeviceID = lc.sDeviceID AND " + "id.sItem = \'" + isItem + "\' ";
            List<Map> tryList1 = fetchRecords(sql + lotSQL);
            if (tryList1.isEmpty()) {
                if (!mzAllowAltLots) {
                    allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                            "Allocation likely failed for one the following reasons:"
                                    + "(1) The location device(s) do not match the " + "aisle controlling device. "
                                    + "(2) There are no item details for item " + isItem + " and lot " + isLot + "\n"
                                    + "NOTE: Alternate lot search is not enabled.");
                } else {
                    tryList1 = fetchRecords(sql);
                    if (tryList1.isEmpty()) {
                        allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                "Allocation likely failed for one the following reasons:"
                                        + "(1) The location device(s) do not match the " + "aisle controlling device. "
                                        + "(2) There are no item details for item " + isItem
                                        + "NOTE: Alternate lot search is not enabled.");
                    }
                }
            }

            if (!tryList1.isEmpty()) {
                sql = sql + lotSQL;
                sql = sql + "AND im.iHoldType = " + DBConstants.ITMAVAIL + " AND " + "id.iHoldType = "
                        + DBConstants.ITMAVAIL + " AND " + "lc.iLocationStatus = " + DBConstants.LCAVAIL + " ";
                List<Map> tryList2 = fetchRecords(sql);
                if (tryList2.isEmpty()) {
                    allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                            "Allocation failed because one or more of the "
                                    + "following must be marked available: Location,"
                                    + "ItemMaster, or Item Detail for item " + isItem);
                } else {
                    sql = sql + "AND ld.iLoadMoveStatus IN (" + DBConstants.NOMOVE + ", " + DBConstants.RETRIEVEPENDING
                            + ", " + DBConstants.RETRIEVING + ") ";
                    List<Map> tryList3 = fetchRecords(sql);
                    if (tryList3.isEmpty()) {
                        allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                "Allocation failed because load with oldest " + "item detail for item " + isItem
                                        + " must have one of the following status\': " + "(1) No Move "
                                        + "(2) Retrieve Pending " + "(3) Retrieving");
                    } else {
                        sql = sql + "AND dv.iOperationalStatus != " + DBConstants.INOP + " ";
                        List<Map> tryList4 = fetchRecords(sql);
                        if (tryList4.isEmpty()) {
                            allocDiag.addProbeDetails("TableJoin.getOldestItemDetails",
                                    "Allocation failed because devices " + "of aisles with item " + isItem + " are not "
                                            + "operational.");
                        }
                    }
                }
            }
        }

        return (idList);
    }

    public List<Map> getDeviceListForBalancedRoundRobin(String isItem, String isLot) throws DBException {
        String sItemSearch = "";

        if (isItem.equals(EBSConstants.EMPTY_TRAY_STACK)) {
            // balance by Empty tray stack in item field
            sItemSearch = " id2.sItem = '" + isItem + "' ";
        } else if (!isLot.isEmpty()) {
            // balance by Flight # in Lot field
            sItemSearch = " id2.sLot = '" + isLot + "' ";
        } else if (isLot.isEmpty()) {
            // balance by Unknown BagID in item field
            sItemSearch = " id2.sItem = '" + isItem + "' ";
        } else {
            String errstr = "EBSTableJoin.getDeviceListForBalancedRoundRobin - Invalid Paramaters.";
            getLogger().logError(errstr);
            return null;
        }

        StringBuilder vpSql = new StringBuilder("SELECT dv.sDeviceID, COUNT(R.sLoadid) as COUNT FROM device dv ")
                .append("LEFT OUTER JOIN (SELECT ld2.* FROM load ld2, LoadLineItem id2 WHERE ")
                .append(" ld2.sLoadid = id2.sLoadid and ").append(sItemSearch)
                .append(" ) R ON dv.sDeviceid = R.sDeviceid ").append(" WHERE ").append(" dv.iPhysicalStatus = ")
                .append(DBConstants.ONLINE).append(" AND ").append(" dv.iOperationalStatus = ")
                .append(DBConstants.APPONLINE).append(" AND ").append(" dv.iDeviceType = ").append(DBConstants.SRC5)
                .append(" GROUP BY dv.sDeviceid ORDER BY COUNT,  ").append("CASE WHEN dv.sDeviceID = 'SR12' THEN 1 ")
                .append("WHEN dv.sDEVICEID = 'SR22' THEN 2 ").append("WHEN dv.sDEVICEID = 'SR32' THEN 3 ")
                .append("WHEN dv.sDEVICEID = 'SR42' THEN 4 ").append("WHEN dv.sDEVICEID = 'SR41' THEN 5 ")
                .append("WHEN dv.sDEVICEID = 'SR31' THEN 6 ").append("WHEN dv.sDEVICEID = 'SR21' THEN 7 ")
                .append("WHEN dv.sDEVICEID = 'SR11' THEN 8 END ");

        List<Map> vpList = fetchRecords(vpSql.toString());

        return (vpList);
    }

    public List<Map> getOrderedDeviceListForEmptyTrays() throws DBException {
        String sItemSearch = "";

        // balance by Empty tray stack in item field
        sItemSearch = " id2.sItem = '" + EBSConstants.EMPTY_TRAY_STACK + "' ";

        StringBuilder vpSql = new StringBuilder("SELECT dv.sDeviceID, COUNT(R.sLoadid) as COUNT FROM device dv ")
                .append("LEFT OUTER JOIN (SELECT ld2.* FROM load ld2, LoadLineItem id2 WHERE ")
                .append(" ld2.sLoadid = id2.sLoadid and ld2.iloadMoveStatus IN (222,223,225) AND ").append(sItemSearch)
                .append(" ) R ON dv.sDeviceid = R.sDeviceid ").append(" WHERE ").append(" dv.iPhysicalStatus = ")
                .append(DBConstants.ONLINE).append(" AND ").append(" dv.iOperationalStatus = ")
                .append(DBConstants.APPONLINE).append(" AND ").append(" dv.iDeviceType = ").append(DBConstants.SRC5)
                .append(" GROUP BY dv.sDeviceid ORDER BY COUNT,  ").append("CASE WHEN dv.sDeviceID = 'SR12' THEN 1 ")
                .append("WHEN dv.sDEVICEID = 'SR22' THEN 2 ").append("WHEN dv.sDEVICEID = 'SR32' THEN 3 ")
                .append("WHEN dv.sDEVICEID = 'SR42' THEN 4 ").append("WHEN dv.sDEVICEID = 'SR41' THEN 5 ")
                .append("WHEN dv.sDEVICEID = 'SR31' THEN 6 ").append("WHEN dv.sDEVICEID = 'SR21' THEN 7 ")
                .append("WHEN dv.sDEVICEID = 'SR11' THEN 8 END ");

        List<Map> vpList = fetchRecords(vpSql.toString());

        return (vpList);
    }

    public double getSumAvailableEmptyTrays() throws DBException {
        StringBuilder vpSql = new StringBuilder(
                "SELECT SUM(id.fCurrentQuantity - id.fAllocatedQuantity) AS \"fTotalQty\" FROM ")
                        .append("Load ld, LoadLineItem id, Location lc, Device dv WHERE ")
                        .append(" ld.sLoadID = id.sLoadID AND ").append(" ld.sWarehouse = lc.sWarehouse AND ")
                        .append(" ld.sAddress = lc.sAddress AND ").append(" lc.sDeviceID = dv.sDeviceID AND ")
                        .append(" id.sItem = '").append(EBSConstants.EMPTY_TRAY_STACK).append("' AND ")
                        .append(" ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE).append(" AND ")
                        .append(" dv.iPhysicalStatus = ").append(DBConstants.ONLINE).append(" AND ")
                        .append(" dv.iOperationalStatus = ").append(DBConstants.APPONLINE).append(" AND ")
                        .append(" lc.iLocationType = ").append(DBConstants.LCASRS).append(" AND ")
                        .append(" lc.iLocationStatus = ").append(DBConstants.LCAVAIL);

        List<Map> vpList = fetchRecords(vpSql.toString());
        double vdTotalQty = 0;

        if (!vpList.isEmpty()) {
            vdTotalQty = DBHelper.getDoubleField(vpList.get(0), "fTotalQty");
        }

        return (vdTotalQty);
    }

    public double getSumUnAvailableEmptyTrays() throws DBException {
        StringBuilder vpSql = new StringBuilder(
                "SELECT SUM(id.fCurrentQuantity - id.fAllocatedQuantity) AS \"fTotalQty\" FROM ")
                        .append("Load ld, LoadLineItem id, Location lc WHERE ").append(" ld.sLoadID = id.sLoadID AND ")
                        .append(" ld.sWarehouse = lc.sWarehouse AND ").append(" ld.sAddress = lc.sAddress AND ")
                        .append(" id.sItem = '").append(EBSConstants.EMPTY_TRAY_STACK).append("' AND ")
                        .append(" ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE).append(" AND ")
                        .append(" lc.iLocationType = ").append(DBConstants.LCASRS).append(" AND ")
                        .append(" lc.iLocationStatus = '").append(DBConstants.LCUNAVAIL);

        List<Map> vpList = fetchRecords(vpSql.toString());
        double vdTotalQty = 0;

        if (!vpList.isEmpty()) {
            vdTotalQty = DBHelper.getDoubleField(vpList.get(0), "fTotalQty");
        }

        return (vdTotalQty);
    }

    public int getEnrouteCountToAisleFromER(String sDest, int iHeight) throws DBException {
        StringBuilder vpSql = new StringBuilder("SELECT COUNT(1) AS \"COUNT\" FROM ")
                .append("PurchaseOrderHeader eh, PurchaseOrderLine el WHERE ").append(" eh.sOrderID = el.sOrderID AND ")
                .append(" eh.sStoreStation = '").append(sDest).append("' ");

        if (iHeight > 0) {
            vpSql.append(" AND el.iHeight = ").append(iHeight);
        }

        List<Map> vpList = fetchRecords(vpSql.toString());
        int vdCount = 0;

        if (!vpList.isEmpty()) {
            vdCount = DBHelper.getIntegerField(vpList.get(0), "COUNT");
        }

        return (vdCount);
    }

    public List<Map> getDataQueueMessagesForWeb() throws DBException {

        StringBuilder vpSql = new StringBuilder(
                "SELECT dmessageaddtime, shostname, 'WRxToHost' AS SDIRECTION,  imessagesequence, imessageprocessed, smessageidentifier, smessage")
                        .append(" FROM asrs.wrxtohost ").append(" UNION ALL ")
                        .append(" SELECT dmessageaddtime, shostname, 'HostToWRx' AS SDIRECTION, imessagesequence, imessageprocessed, smessageidentifier, smessage  ")
                        .append(" FROM asrs.hosttowrx ").append(" ORDER BY dmessageaddtime DESC ");

        List<Map> vpList = fetchRecords(vpSql.toString());

        return (vpList);
    }

    public List<Map> getLoadListWeb(LoadData loadData, String isItem, String flightId) throws DBException {
        /*
         * -- Sample for Debug
         *
         */
        StringBuilder vpSQL = new StringBuilder();

        vpSQL.append("SELECT * FROM VIEW_LOADSCREEN WHERE 1=1 ");

        if (isItem != null)
            vpSQL.append(" AND sItem LIKE '%").append(isItem).append("%' ");

        if ((loadData.getWarehouse() != null) && (!loadData.getWarehouse().isEmpty()))
            vpSQL.append(" AND sWarehouse = '").append(loadData.getWarehouse()).append("' ");

        if ((loadData.getAddress() != null) && (!loadData.getAddress().isEmpty()))
            vpSQL.append(" AND sAddress LIKE '%").append(loadData.getAddress()).append("%' ");

        if ((loadData.getDeviceID() != null) && (!loadData.getDeviceID().isEmpty()))
            vpSQL.append(" AND sDeviceId = '").append(loadData.getDeviceID()).append("' ");

        if ((loadData.getLoadID() != null) && (!loadData.getLoadID().isEmpty()))
            vpSQL.append(" AND sLoadId LIKE '%").append(loadData.getLoadID()).append("%' ");

        if (flightId != null)
            vpSQL.append(" AND sLot ='").append(flightId).append("' ");

        return fetchRecords(vpSQL.toString());
    }

    public List<Map> getWorkListWeb(MoveCommandData moveCommandData, String isItem, String flightId) throws DBException {
       
        StringBuilder vpSQL = new StringBuilder();

        vpSQL.append("SELECT * FROM VIEW_WORK_MAINTENANCE WHERE 1=1 ");

        if (isItem != null)
            vpSQL.append(" AND SITEMID LIKE '%").append(isItem).append("%' ");

        if (!(moveCommandData.getDeviceID().equals("ALL")))
            vpSQL.append(" AND sDeviceId = '").append(moveCommandData.getDeviceID()).append("' ");

        if ((moveCommandData.getLoadID() != null) && (!moveCommandData.getLoadID().isEmpty()))
            vpSQL.append(" AND sLoadId LIKE '%").append(moveCommandData.getLoadID()).append("%' ");

        if (flightId != null)
            vpSQL.append(" AND SFLIGHTNUM ='").append(flightId).append("' ");

        return fetchRecords(vpSQL.toString());
    }
    /**
     * Get the PO's with the specific POSearch, POType, Item, Lot on them
     *
     * @return <code>List</code> of Purchase Order Headers that match the criteria
     */
    public List<Map> getPOSearchList(PurchaseOrderHeaderData pohdata, PurchaseOrderLineData poldata)
            throws DBException {
        subquery1.setLength(0);
        subquery2.setLength(0);

        subquery1.append(DBHelper.buildWhereClause(pohdata.getKeyArray(), "POH", true));
        subquery2.append(DBHelper.buildWhereClause(poldata.getKeyArray(), "POL", true));

        StringBuilder vpSql = new StringBuilder("SELECT DISTINCT POH.*, pol.sLineId,pol.sItem, pol.sLot, pol.sLoadid ")
                .append("FROM PURCHASEORDERHEADER POH, PURCHASEORDERLINE POL ")
                .append(" WHERE POH.SORDERID = POL.SORDERID ");
        if (subquery1.length() > 1) // buildWhereClause may return a " "
        {
            vpSql.append(" AND ").append(subquery1);
        }
        if (subquery2.length() > 1) // buildWhereClause may return a " "
        {
            vpSql.append(" AND ").append(subquery2);
        }
        vpSql.append(" ORDER BY POH.SORDERID");

        return fetchRecords(vpSql.toString());
    }

    /**
     * Method to return the other otuput station by device (only used to determine lower output)
     *
     * @throws DBException for DB access errors.
     */
    public String getAlternateOutputByDevice(String isDeviceID, String isOutputStation) throws DBException {
        StringBuilder vpSql = new StringBuilder("SELECT sStationName FROM STATION  ").append(" WHERE iStationType = ")
                .append(DBConstants.OUTPUT).append(" ").append(" AND sDeviceID = '").append(isDeviceID).append("' ")
                .append(" AND sStationName != '").append(isOutputStation).append("' ");

        String vsOutputStn = "";
        List<Map> vpList = fetchRecords(vpSql.toString());
        if (!vpList.isEmpty()) {
            vsOutputStn = DBHelper.getStringField(vpList.get(0), StationData.STATIONNAME_NAME);
        }

        return vsOutputStn;
    }

    // Get the Flight with Flight and Bag count
    public List<Map> getFlightList() throws DBException {

        StringBuilder vpSql = new StringBuilder(" SELECT b.sLot as Flights, count(1) BagQuantity, min(b.dExpectedDate) as FlightSTD, min(b.dExpirationDate) as FlightETD ")
                .append(" FROM LOAD a ").append(" INNER JOIN LOADLINEITEM b ON a.sLoadId = b.sLoadId ")
                .append(" WHERE  b.sLot IS NOT NULL").append(" GROUP BY b.slot ");

        List<Map> vpList = fetchRecords(vpSql.toString());
        return vpList;
    }

    // Get the Flight Detail with Flight Id
    public List<Map> getFlightDetailsList(String flightId) throws DBException {

        StringBuilder vpSql = new StringBuilder(" SELECT a.*, b.sLot, b.sLineId, b.sItem ").append(" FROM LOAD a ")
                .append(" INNER JOIN LOADLINEITEM b ON a.sLoadId = b.sLoadId ").append(" WHERE  b.sLot IS NOT NULL ")
                .append(" AND b.sLot = '").append(flightId).append("' ").append(" ORDER BY a.iID DESC ");

        List<Map> vpList = fetchRecords(vpSql.toString());
        return vpList;
    }

    // Get the List of Schema IDs
    public Map<Integer, String> getAllSchemaIDList() throws DBException {

        StringBuilder vpSql = new StringBuilder(" SELECT a.iSchemaID, a.sName ").append(" FROM TIMESLOTSCHEMADEF a ")
                .append(" ORDER BY a.iSchemaID ");

        List<Map> vpList = fetchRecords(vpSql.toString());
        Map<Integer, String> vpSchemaMap = new LinkedHashMap<>();

        for (Map vpRowMap : vpList) {
            int vsSchemaId = DBHelper.getIntegerField(vpRowMap, "iSchemaID");
            String vsName = DBHelper.getStringField(vpRowMap, "sName");
            vpSchemaMap.put(vsSchemaId, vsName);
        }
        return (vpSchemaMap);
    }

    /**
     * This Methods is called from TIMER to get the latest Expected Receipts arrived from Host (SAC)and need to be
     * processed
     * 
     * @return
     * @throws DBException
     */
    public List<Map> getLatestPurchaseOrdersToProcess() throws DBException {

        StringBuilder vpSql = new StringBuilder(
                " select po.sOrderId as GlobalId,po.dExpectedDate as  FlightSchduleDate ,po.sStoreStation as FinalSortLocation,po.sAddMethod as RequestType,pl.sLoadId as TrayId, ")
                        .append(" pl.sItem as ItemId,pl.sLot as FlightNum,pl.dExpirationDate as DefaultRetrievalDate,pl.iInspection as  ItemType from PURCHASEORDERHEADER as po ")
                        .append(" inner join PURCHASEORDERLINE as pl on po.sOrderId = pl.sOrderId ")
                        .append(" where po.iPurchaseOrderStatus = " + DBConstants.ERBUILDING) // 24
                        .append(" order by po.dLastActivityTime ");

        List<Map> vpList = fetchRecords(vpSql.toString());

        return (vpList);
    }

    public List<Map> getExpiredBagLoads() throws DBException {
        StringBuilder vpSql = new StringBuilder(
                " SELECT DISTINCT ld.[IID], ld.[SPARENTLOAD], ld.[SLOADID], ld.[SWAREHOUSE], ld.[SADDRESS], ld.[SROUTEID], ld.[SDEVICEID], ")
                        .append(" li.[IID] AS [LOADLINEID], li.[SITEM], li.[SLOT], li.[SORDERID], li.[DEXPIRATIONDATE]")
                        .append(" FROM  asrs.LOAD ld WITH (nolock) LEFT JOIN asrs.LoadLineItem li WITH (nolock) ON ld.[SLOADID] = li.[SLOADID]")
                        .append(" WHERE li.[DEXPIRATIONDATE] <= GetDate()")
                        .append(" AND ld.iLoadMoveStatus = " + DBConstants.NOMOVE) // 224
                        .append(" AND li.[SORDERID] IS NULL ").append(" ORDER BY ld.[SADDRESS] ");

        List<Map> vpList = fetchRecords(vpSql.toString());
        return (vpList);
    }
    
    //returns purchase order which is received more than 5 hrs before and not completed
    public List<Map> getNotCompletedPO() throws DBException {
        StringBuilder vpSql = new StringBuilder(
                " SELECT *")
                        .append(" FROM  asrs.PURCHASEORDERHEADER")
                        .append(" WHERE DATEDIFF(HOUR,dExpectedDate,GETDATE())>")
                        .append(" 5")
                        .append(" AND [iPurchaseOrderStatus] = " + DBConstants.EREXPECTED) // 27
                        ;

        List<Map> vpList = fetchRecords(vpSql.toString());
        return (vpList);
    }

    /**
     * Return the map of number of load(already stored or to be stored) of the lot per device id
     * 
     * @param warehouse warehouse name, for example, "EBS"
     * @param lotId lot id, for example, flight number like "ANZ 101"
     * @param flightScheduledDateTime flight scheduled date time
     * @return the map of number of load of the lot per device id
     * @throws DBException When anything goes wrong with the join query
     */
    public Map<String, Integer> getNumberOfLoadPerDeviceId(String warehouse, String lotId, Date flightScheduledDateTime)
            throws DBException {
        Map<String, Integer> resultMap = new LinkedHashMap<String, Integer>();

        // Get the device id list
        StringBuilder strBuilder1 = new StringBuilder()
                .append("SELECT sDeviceID ")
                .append("  FROM DEVICE ")
                .append(" WHERE sWarehouse = '").append(warehouse).append("' ")
                .append("   AND iOperationalStatus != ")
                .append(DBConstants.INOP).append(" ORDER BY iAisleGroup ASC ");
        List<Map> list1 = fetchRecords(strBuilder1.toString());

        for (Map map : list1) {
            String deviceIdName = DBHelper.getStringField(map, LoadData.DEVICEID_NAME);
            resultMap.put(deviceIdName, 0);
        }

        // Count the number of loads
        StringBuilder strBuilder2 = new StringBuilder()
                .append("     SELECT ld.sDeviceID, count(*) AS loadCount")
                .append("       FROM LOADLINEITEM AS lli")
                .append(" INNER JOIN LOAD AS ld")
                .append("         ON ld.sLoadID = lli.sLoadID")
                .append("        AND ld.sWarehouse = '").append(warehouse).append("'")
                .append(" INNER JOIN LOCATION AS lc")
                .append("         ON lc.sAddress = ld.sAddress")
                .append("        AND lc.sDeviceID = ld.sDeviceID")
                .append("        AND lc.sWarehouse = ld.sWarehouse")
                .append("        AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
                .append("        AND (lc.iEmptyFlag = ").append(DBConstants.OCCUPIED).append(" OR lc.iEmptyFlag = ").append(DBConstants.LCRESERVED).append(") ")
                .append("        AND LEFT(lc.sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'")
                .append(" INNER JOIN DEVICE AS dv")
                .append("         ON dv.sDeviceID = ld.sDeviceID")
                .append("        AND dv.iOperationalStatus != ").append(DBConstants.INOP)
                .append("      WHERE lli.sLot = '").append(lotId).append("'")
                .append("        AND lli.dExpectedDate = ").append(DBHelper.convertDateToDBString(flightScheduledDateTime))
                .append("   GROUP BY ld.sDeviceID")
                .append("   ORDER BY count(*) ASC");
        List<Map> list2 = fetchRecords(strBuilder2.toString());

        // Apply the count result to the device list
        for (Map map : list2) {
            String deviceIdName = DBHelper.getStringField(map, LoadData.DEVICEID_NAME);
            Integer loadCount = DBHelper.getIntegerField(map, "loadCount");
            resultMap.put(deviceIdName, loadCount);
        }

        return resultMap;
    }

    /**
     * Return the map of number of load of the lot per bank
     * 
     * @param warehouse warehouse name, for example, "EBS"
     * @param lotId lot id, for example, flight number like "ANZ 101"
     * @param deviceId device id, for example, "SR11"
     * @return the map of number of load of the lot per bank
     * @throws DBException When anything goes wrong with the join query
     */
    public Map<String, Integer> getNumberOfLoadPerBank(String warehouse, String lotId, String deviceId)
            throws DBException {
        Map<String, Integer> resultMap = new LinkedHashMap<String, Integer>();

        // Get the list of bank number like 001, 002 of the device id
        // Currently, address is type 2 digits(20 = storage location) + bank 3 digits + bay 3 digits + level 2 digits
        StringBuilder strBuilder1 = new StringBuilder()
                .append("   SELECT DISTINCT SUBSTRING(sAddress, 3, 3) AS bank")
                .append("     FROM LOCATION")
                .append("    WHERE sWarehouse = '").append(warehouse).append("'")
                .append("      AND sDeviceID = '").append(deviceId).append("'")
                .append("      AND iLocationStatus = ").append(DBConstants.LCAVAIL)
                .append("      AND LEFT(sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'")
                .append(" ORDER BY SUBSTRING(sAddress, 3, 3) ASC");
        List<Map> bankList = fetchRecords(strBuilder1.toString());
        for (Map bank : bankList) {
            String bankName = DBHelper.getStringField(bank, "bank");
            resultMap.put(bankName, 0);
        }

        // Count the number of loads
        StringBuilder strBuilder2 = new StringBuilder()
                .append("     SELECT SUBSTRING(lc.sAddress, 3, 3) AS bank, COUNT(*) AS loadCount")
                .append("       FROM LOAD AS ld")
                .append(" INNER JOIN LOADLINEITEM AS li")
                .append("         ON li.sLoadID = ld.sLoadID")
                .append("        AND li.sLot = '").append(lotId).append("'")
                .append(" INNER JOIN LOCATION AS lc")
                .append("         ON lc.sAddress = ld.sAddress")
                .append("        AND lc.sWarehouse = ld.sWarehouse")
                .append("        AND lc.sDeviceID = ld.sDeviceID")
                .append("        AND lc.sWarehouse = '").append(warehouse).append("'")
                .append("        AND lc.sDeviceID = '").append(deviceId).append("'")
                .append("        AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
                .append("        AND (lc.iEmptyFlag = ").append(DBConstants.OCCUPIED).append(" OR lc.iEmptyFlag = ").append(DBConstants.LCRESERVED).append(") ")
                .append("        AND LEFT(lc.sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE).append("'")
                .append("      WHERE ld.sWarehouse = '").append(warehouse).append("'")
                .append("        AND ld.sDeviceID = '").append(deviceId).append("'")
                .append("   GROUP BY SUBSTRING(lc.sAddress, 3, 3) ");
        List<Map> loadPerBankList = fetchRecords(strBuilder2.toString());

        // Apply the count result to the bank list
        for (Map load : loadPerBankList) {
            String bankName = DBHelper.getStringField(load, "bank");
            Integer loadCount = DBHelper.getIntegerField(load, "loadCount");
            resultMap.put(bankName, loadCount);
        }

        return resultMap;
    }

    /**
     * This is to get retrievable entries of loadlineitem table
     * 
     * @param warehouse Warehouse name
     * @param lotId Lot Id/Flight number
     * @param flightScheduledDateTime Flight scheduled datetime
     * @param deviceId Null if you want to get retrievable entries of loadlineitem table of all devices and not null if
     *        not
     * @param numberOfRows Null if you want to get all retrievable entries of loadlineitem table and not null when you
     *        want to retrieve up to the number
     * @return The list of LoadLineItemData
     * @throws DBException If anyting goes wrong
     */
    public List<LoadLineItemData> getAllRetrievableLoadLineItemsForScheduledFlight(String warehouse, String lotId,
            Date flightScheduledDateTime, String deviceId, Short numberOfRows) throws DBException {
        StringBuilder queryBuf = new StringBuilder();
        queryBuf.append(" SELECT ");
        if (numberOfRows != null &&  numberOfRows > 0) {
            queryBuf.append(" TOP ").append(numberOfRows);
        }
        queryBuf.append(" lli.*");
        queryBuf.append(" FROM LOADLINEITEM AS lli");
        
        queryBuf.append(" INNER JOIN LOAD AS ld");
        queryBuf.append("         ON ld.sLoadID = lli.sLoadID");
        queryBuf.append("        AND ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE);
        queryBuf.append("        AND ld.sWarehouse = '").append(warehouse).append("'");
        if (deviceId != null) {
            queryBuf.append("        AND ld.sDeviceID = '").append(deviceId).append("'");
        }
        queryBuf.append(" INNER JOIN LOCATION AS lc");
        queryBuf.append("         ON lc.sAddress = ld.sAddress");
        queryBuf.append("        AND lc.sDeviceID = ld.sDeviceID");
        queryBuf.append("        AND lc.sWarehouse = ld.sWarehouse");
        queryBuf.append("        AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL);
       //queryBuf.append("        AND lc.iEmptyFlag = ").append(DBConstants.OCCUPIED);
   
        queryBuf.append(" INNER JOIN DEVICE AS dv");
        queryBuf.append("         ON dv.sDeviceID = ld.sDeviceID");
        queryBuf.append("        AND dv.iOperationalStatus != ").append(DBConstants.INOP);
        queryBuf.append("      WHERE lli.iHoldType = ").append(DBConstants.ITMAVAIL);
        queryBuf.append("      AND lli.dExpectedDate = ");
        queryBuf.append(DBHelper.convertDateToDBString(flightScheduledDateTime));
        queryBuf.append("        AND lli.sLot = '").append(lotId).append("'");
        queryBuf.append("   ORDER BY ld.sAddress ASC");
        
        List<Map> results = fetchRecords(queryBuf.toString());
        if (results == null || results.isEmpty()) {
            return new ArrayList<LoadLineItemData>();
        }

        List<LoadLineItemData> loadLineItemDataList = results.stream().map(row -> {
            LoadLineItemData liToReturn = Factory.create(LoadLineItemData.class);
            liToReturn.dataToSKDCData(row);
            return liToReturn;
        }).collect(Collectors.toList());

        return loadLineItemDataList;
    }
    
    public List<LoadLineItemData> getAllRetrievableLoadLineItems(List<String> loadId) throws DBException{
    	 StringBuilder queryBuf = new StringBuilder();
         queryBuf.append(" SELECT * from LOADLINEITEM where sLoadID in ( "+String.join(",", loadId)+")");
         
    	List<Map> results = fetchRecords(queryBuf.toString());
        if (results == null || results.isEmpty()) {
            return new ArrayList<LoadLineItemData>();
        }

        List<LoadLineItemData> loadLineItemDataList = results.stream().map(row -> {
            LoadLineItemData liToReturn = Factory.create(LoadLineItemData.class);
            liToReturn.dataToSKDCData(row);
            return liToReturn;
        }).collect(Collectors.toList());

        return loadLineItemDataList;
    }
	
    /**
     * This returns the number of entries of loadlineitem per deviceID
     * 
     * @param warehouse Warehouse name
     * @param lotId Lot Id/Flight number
     * @param flightScheduledDateTime Flight scheduled datetime
     * @return The number of entries of loadlineitem per deviceID
     * @throws DBException If anything goes wrong
     */
    public Map<String, Integer> getNumberOfRetrievableLoadLineItemsPerDeviceForScheduledFlight(String warehouse,
            String lotId, Date flightScheduledDateTime) throws DBException {
        Map<String, Integer> resultMap = new LinkedHashMap<String, Integer>();

        StringBuilder queryBuf = new StringBuilder()
                .append("     SELECT dv.sDeviceID as device, count(*) AS loadCount")
                .append("       FROM LOADLINEITEM AS lli")
                .append(" INNER JOIN PURCHASEORDERHEADER AS poh")
                .append("         ON poh.sOrderID = lli.sOrderID")
                .append("        AND poh.dExpectedDate = ")
                .append(DBHelper.convertDateToDBString(flightScheduledDateTime))
                .append(" INNER JOIN LOAD AS ld")
                .append("         ON ld.sLoadID = lli.sLoadID")
                .append("        AND ld.iLoadMoveStatus = ").append(DBConstants.NOMOVE)
                .append("        AND ld.sWarehouse = '").append(warehouse).append("'")
                .append(" INNER JOIN LOCATION AS lc")
                .append("         ON lc.sAddress = ld.sAddress")
                .append("        AND lc.sDeviceID = ld.sDeviceID")
                .append("        AND lc.sWarehouse = ld.sWarehouse")
                .append("        AND lc.iLocationStatus = ").append(DBConstants.LCAVAIL)
                .append("        AND lc.iEmptyFlag = ").append(DBConstants.OCCUPIED)
                .append("        AND LEFT(lc.sAddress, 2) = '").append(SACControlMessage.LOCATION_STORAGE_TYPE)
                .append("'") // 20 = Storage location
                .append(" INNER JOIN DEVICE AS dv")
                .append("         ON dv.sDeviceID = ld.sDeviceID")
                .append("        AND dv.iOperationalStatus != ").append(DBConstants.INOP)
                .append("      WHERE lli.iHoldType = ").append(DBConstants.ITMAVAIL)
                .append("        AND lli.fAllocatedQuantity = 0")
                .append("        AND lli.fCurrentQuantity > 0")
                .append("        AND lli.sLot = '").append(lotId).append("'")
                .append("   GROUP BY dv.sDeviceID")
                .append("   ORDER BY count(*) ASC");

        List<Map> loadPerDeviceList = fetchRecords(queryBuf.toString());
        for (Map load : loadPerDeviceList) {
            String deviceID = DBHelper.getStringField(load, "device");
            Integer loadCount = DBHelper.getIntegerField(load, "loadCount");
            resultMap.put(deviceID, loadCount);
        }

        return resultMap;
    }

    /**
     * Update location EmptyFlag status
     * 
     * @param saddress
     * @param status
     */
    public void updateLocationEmptyFlag(String saddress, int status) {
        try {
            this.execute("Update Location set iEmptyFlag=? WHERE sAddress = ?", status, saddress);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }
   
    public void deleteMoveByLoadId(String sLoadId) {
        try {
            this.execute("delete from MOVE  WHERE sLoadID = ? ", sLoadId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public void deleteLoadByLoadId(String sLoadId) {
        try {
            this.execute("delete from LOAD  WHERE sLoadID = ? ", sLoadId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public void deleteLoadlineByLoadId(String sLoadId) {
        try {
            this.execute("delete from LOADLINEITEM  WHERE sLoadID = ? ", sLoadId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public void deletePurchaseOrderLineByOrderId(String isOrderId) {
        try {
            this.execute("delete from PURCHASEORDERLINE  WHERE sOrderID = ? ", isOrderId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public void deletePurchaseOrderHeaderByOrderId(String isOrderId) {
        try {
            this.execute("delete from PURCHASEORDERHEADER  WHERE sOrderID = ? ", isOrderId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }
    public void deleteOrderLineByLoadId(String isLoadId) {
        try {
            this.execute("delete from ORDERLINE WHERE sLoadID = ? ", isLoadId);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }
    
    /**
	 * Method to delete all the move commands for load
	 * @param loadId - uniquely identify the move command
	 * @throws DBException When anything goes wrong
	 */
	public void deleteMoveCommand(String loadId){
		try {
			this.execute("delete from MoveCommand where sLoadID = ? ", loadId);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}
	
    public void updateLoadLineItemOrderId(String isLoadId,String isOrderId) {
        try {
        	this.execute("update LOADLINEITEM set sOrderID = ?  WHERE sLoadID = ? ",isOrderId, isLoadId);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }
    
    
    public void updateOrderStatusById(String isOrderId,int iStatus) {
        try {
        	String Sql = " update ORDERHEADER set iOrderStatus = "+iStatus+" WHERE sOrderID = '"+isOrderId+"' ";
        	this.execute(Sql);
            //this.execute("update ORDERHEADER set iOrderStatus = ?  WHERE sOrderID = ? ",iStatus, isOrderId);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }
    
    public void deleteOrderLineByOrderId(String isOrderId) {
        try {
            this.execute("delete from ORDERLINE WHERE sOrderID = ? ", isOrderId);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public void deleteOrderHeaderByOrderId(String isOrderId) {
        try {
            //delete only if no child exist
        	this.execute("delete from ORDERHEADER where ORDERHEADER.sOrderId = ? and NOT EXISTS(SELECT 1 FROM ORDERLINE ol WHERE ORDERHEADER.sOrderId = ol.sOrderId)",isOrderId);
        	//this.execute("delete from ORDERHEADER WHERE sOrderID = ? ", isOrderId);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public String getOrderHeaderIdByLoadId(String isLoadID) throws DBException {
        StringBuilder vpSql = new StringBuilder("SELECT sOrderId from ORDERLINE WHERE sLoadID = '").append(isLoadID)
                .append("'");
        List<Map> vpData = fetchRecords(vpSql.toString());

        return ((!vpData.isEmpty()) ? DBHelper.getStringField(vpData.get(0), "SORDERID") : null);
    }

    public String getPOIdByLoadId(String isLoadID) throws DBException {
        StringBuilder vpSql = new StringBuilder("SELECT sOrderId from PURCHASEORDERLINE WHERE sLoadID = '")
                .append(isLoadID).append("'");
        List<Map> vpData = fetchRecords(vpSql.toString());

        return ((!vpData.isEmpty()) ? DBHelper.getStringField(vpData.get(0), "SORDERID") : null);

    }

    public void setLocationEmptyStatus(String sAddress, int iEmptyFlag) {
        try {
            this.execute("update LOCATION set iEmptyFlag = ? where sAddress = ? ", iEmptyFlag, sAddress);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public void updateLoadLineOrderId(String LoadId, String orderId) {
        try {
            this.execute("update LOADLINEITEM set sOrderID = ? where sLoadID = ? ", orderId, LoadId);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }    
    public String getDestinationIdFromRoute(String sFromId) throws DBException {
        StringBuilder vpSql = new StringBuilder(" select sDestID from ROUTE where sFromID = '").append(sFromId)
                .append("'");
        List<Map> vpData = fetchRecords(vpSql.toString());
        return ((!vpData.isEmpty()) ? DBHelper.getStringField(vpData.get(0), "SDESTID") : null);
    }

    public List<String> getDevicesWithLoadsOfFlight(String warehouse, String lotId) throws DBException {
        List<String> result = new ArrayList<>();
        
        StringBuilder strBuilder = new StringBuilder()
                .append("     SELECT DISTINCT(ld.sDeviceID)")
                .append("       FROM LOADLINEITEM AS lli")
                .append(" INNER JOIN LOAD AS ld")
                .append("         ON ld.sLoadID = lli.sLoadID")
                .append("        AND ld.sWarehouse = '").append(warehouse).append("'")
                .append(" INNER JOIN DEVICE AS dv")
                .append("         ON dv.sDeviceID = ld.sDeviceID")
                .append("        AND dv.iOperationalStatus != ").append(DBConstants.INOP)
                .append("      WHERE lli.sLot = '").append(lotId).append("'");
        List<Map> list = fetchRecords(strBuilder.toString());
        for (Map map : list) {
            String deviceIdName = DBHelper.getStringField(map, LoadData.DEVICEID_NAME);
            result.add(deviceIdName);
        }

        return result;
    }

    public String getDeviceWithLoad(String warehouse, String loadID) throws DBException {
        StringBuilder strBuilder = new StringBuilder()
                .append("     SELECT DISTINCT(ld.sDeviceID)")
                .append("       FROM LOAD AS ld")
                .append(" INNER JOIN DEVICE AS dv")
                .append("         ON dv.sDeviceID = ld.sDeviceID")
                .append("        AND dv.iOperationalStatus != ").append(DBConstants.INOP)
                .append("      WHERE ld.sLoadID = '").append(loadID).append("'")
                .append("        AND ld.sWarehouse = '").append(warehouse).append("'");
        List<Map> list = fetchRecords(strBuilder.toString());
        if(!list.isEmpty()) {
            return DBHelper.getStringField(list.get(0), LoadData.DEVICEID_NAME);
        }

        return null;
    }
    
    public String getDeviceForLocationAddress(String sAddress) throws DBException {
    	StringBuilder vpSQL = new StringBuilder();

        vpSQL.append("SELECT * from location WHERE iLocationType = ").append(DBConstants.LCASRS);
        vpSQL.append(" AND sAddress = '").append(sAddress).append("' ");
        List<Map> list = fetchRecords(vpSQL.toString());
        if(!list.isEmpty())
        	return DBHelper.getStringField(list.get(0), LocationData.DEVICEID_NAME);
        return null;
    }

	public List<Map> getLocationData(LocationData locationData) throws DBException {
		StringBuilder vpSQL = new StringBuilder();

        vpSQL.append("SELECT * from location WHERE iLocationType = ").append(DBConstants.LCASRS);

        if (locationData.getWarehouse() != null && (!locationData.getWarehouse().isEmpty()))
            vpSQL.append(" AND sWarehouse = '").append(locationData.getWarehouse()).append("' ");
        
        if (locationData.getAddress() != null && (!locationData.getAddress().isEmpty()))
            vpSQL.append(" AND sAddress = '").append(locationData.getAddress()).append("' ");
        
        if (locationData.getDeviceID() != null && (!locationData.getDeviceID().isEmpty()))
            vpSQL.append(" AND sDeviceID = '").append(locationData.getDeviceID()).append("' ");
        
        if (locationData.getZone() != null && (!locationData.getZone().isEmpty()))
            vpSQL.append(" AND sZone = '").append(locationData.getZone()).append("' ");
        
        if (locationData.getLocationStatus() != DBConstants.LCAVAIL)
            vpSQL.append(" AND iLocationStatus = ").append(locationData.getLocationStatus());
        
        if (locationData.getEmptyFlag() != DBConstants.UNOCCUPIED)
            vpSQL.append(" AND iEmptyFlag = ").append(locationData.getEmptyFlag());
        
        if (locationData.getLocationType() != DBConstants.LCASRS)
            vpSQL.append(" AND iLocationType = ").append(locationData.getLocationType());
       
        List<Map> list = fetchRecords(vpSQL.toString());
        return list;
	}

	public List<Map> getEquipmentsData(LocationData locationData) throws DBException {
		StringBuilder vpSQL = new StringBuilder();

        vpSQL.append("SELECT * from location WHERE iLocationType not in (10,15,19) ");

        if (locationData.getWarehouse() != null && (!locationData.getWarehouse().isEmpty()))
            vpSQL.append(" AND sWarehouse = '").append(locationData.getWarehouse()).append("' ");
        
        if (locationData.getAddress() != null && (!locationData.getAddress().isEmpty()))
            vpSQL.append(" AND sAddress = '").append(locationData.getAddress()).append("' ");
        
        if (locationData.getDeviceID() != null && (!locationData.getDeviceID().isEmpty()))
            vpSQL.append(" AND sDeviceID = '").append(locationData.getDeviceID()).append("' ");
        
        if (locationData.getZone() != null && (!locationData.getZone().isEmpty()))
            vpSQL.append(" AND sZone = '").append(locationData.getZone()).append("' ");
        
        List<Map> list = fetchRecords(vpSQL.toString());
        return list;

	}

	public List<Map> getOccupancyData(OccupancyData ipLocKey) throws DBException {
		StringBuilder vpSQL = new StringBuilder();
        vpSQL.append("SELECT * from occupancy order by dLastMovementTime desc");
        List<Map> list = fetchRecords(vpSQL.toString());
        return list;
	}
	
	 public String getLoadIddByPOI(String isOrderId) throws DBException {
	        StringBuilder vpSql = new StringBuilder("SELECT sLoadID from PURCHASEORDERLINE WHERE sOrderID = '")
	                .append(isOrderId).append("'");
	        List<Map> vpData = fetchRecords(vpSql.toString());
	        return ((!vpData.isEmpty()) ? DBHelper.getStringField(vpData.get(0), "SLOADID") : null);
	    }

	public boolean checkLoadMoveStatus(String isLoadId) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT iLoadMoveStatus from LOAD WHERE sLoadID = '")
                .append(isLoadId).append("'");
        List<Map> vpData = fetchRecords(vpSql.toString());
        int status = DBHelper.getIntegerField(vpData.get(0), "ILOADMOVESTATUS");
		if(status == DBConstants.ARRIVEPENDING) {
			return true;
		}
		return false;
	}

	public String getAddressByLoadId(String isLoadId) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT sAddress from LOAD WHERE sLoadID = '")
                .append(isLoadId).append("'");
        List<Map> vpData = fetchRecords(vpSql.toString());
        return ((!vpData.isEmpty()) ? DBHelper.getStringField(vpData.get(0), "SADDRESS") : null);
	}

	public boolean checkPOStatus(String isOrderId) throws DBException {
		StringBuilder vpSql = new StringBuilder("SELECT iPurchaseOrderStatus from PURCHASEORDERHEADER WHERE sOrderID = '")
                .append(isOrderId).append("'");
        List<Map> vpData = fetchRecords(vpSql.toString());
        int status = DBHelper.getIntegerField(vpData.get(0), "IPURCHASEORDERSTATUS");
		if(status != DBConstants.ERCOMPLETE) {
			return true;
		}
		return false;
	}

	//returns purchase order line of not completed purchase order header
	public List<Map> getNotCompletedPOL(String sOrderId) throws DBException {
		StringBuilder vpSql = new StringBuilder(
                " SELECT *")
                        .append(" FROM  asrs.PURCHASEORDERLINE")
                        .append(" WHERE sOrderID=")
                        .append(sOrderId);

        List<Map> vpList = fetchRecords(vpSql.toString());
        return (vpList);
	}

	
	//returns Load by loadid
	public List<Map> getLOAD(String sLoadID) throws DBException {
		StringBuilder vpSql = new StringBuilder(
                " SELECT *")
                        .append(" FROM  asrs.LOAD")
                        .append(" WHERE sLoadID=")
                        .append(sLoadID);

        List<Map> vpList = fetchRecords(vpSql.toString());
        return (vpList);
	}
	
	//returns Loadlineitem by loadid
		public List<Map> getLOADLINEITEM(String sLoadID) throws DBException {
			StringBuilder vpSql = new StringBuilder(
	                " SELECT *")
	                        .append(" FROM  asrs.LOADLINEITEM")
	                        .append(" WHERE sLoadID=")
	                        .append(sLoadID);

	        List<Map> vpList = fetchRecords(vpSql.toString());
	        return (vpList);
		}

	//delete LOAD and LOADLINE by loadid
	@Transactional
	public void deleteLLIandLOAD(String sLoadID){
		try {
			this.execute("DELETE from LOADLINEITEM where sLoadID = ?", sLoadID);
			this.execute("DELETE from LOAD where sLoadID = ?", sLoadID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//return LOCATION by sAddress
	public List<Map> getlocationBySAddress(String sAddress) throws DBException {
		StringBuilder vpSql = new StringBuilder(
                " SELECT *")
                        .append(" FROM  asrs.LOCATION")
                        .append(" WHERE sAddress=")
                        .append(sAddress);

        List<Map> vpList = fetchRecords(vpSql.toString());
        return (vpList);
	}

	//changes status of LOCATION to available from arrival pending
	public void changeStatusOfLocation(Map map) {
        try {
        	System.out.println("success");
			this.execute("UPDATE LOCATION set iLocationStatus = ? where iID = ? ", (Integer)29, map.get("IID"));
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	//set PURCHASEORDER to completed state
	public void setPOtoComplete(Map vpLoadMap) {
		try {
			this.execute("UPDATE PURCHASEORDERHEADER set iPurchaseOrderStatus = ? where iID = ? ", (Integer)26, vpLoadMap.get("IID"));
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public void addToTransactionHistory(Map POL, Map LOAD, Map LOCATION, Map LLI) {
		try {
			System.out.println("start");
			this.execute("INSERT INTO TRANSACTIONHISTORY ("
					+ "      [dTransDateTime]\r\n"
					+ "      ,[sItem]\r\n"
					+ "      ,[sLot]\r\n"
					+ "      ,[sLoadID]\r\n"
					+ "      ,[sDeviceID]\r\n"
					+ "      ,[sRouteID]\r\n"
					+ "      ,[iAisleGroup]\r\n"
					+ "      ,[dLastCCIDate]\r\n"
					+ "      ,[dAgingDate]\r\n"
					+ "      ,[fCurrentQuantity]\r\n"
					+ "      ,[fReceivedQuantity]\r\n"
					+ "      ,[sOrderID]\r\n"
					+ "      ,[sLineID]\r\n"
					+ "      ,[sOrderLot]\r\n"
					+ "      ,[iHoldType]\r\n"
					+ "      ,[dModifyTime]\r\n"
					+ "      ,[sAddMethod]\r\n"
					+ "      ,[sUpdateMethod]\r\n "
					+ "      ,[iTranCategory]\r\n"
					+ "      ,[sMachineName]\r\n"
					+ "      ,[iTranType])"
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
					,new Date(),POL.get("SITEM"),LLI.get("SLOT"),LOAD.get("SLOADID")
					,LOAD.get("SDEVICEID"),POL.get("SROUTEID"),LOCATION.get("IAISLEGROUP"),LLI.get("DLASTCCIDATE")
					,LLI.get("DAGINGDATE"),LLI.get("FCURRENTQUANTITY"),POL.get("FRECEIVEDQUANTITY")
					,POL.get("SORDERID"),POL.get("SLINEID"),LLI.get("SORDERLOT"),LLI.get("IHOLDTYPE")
					,new Date(),LOAD.get("SADDMETHOD"),LOAD.get("SUPDATEMETHOD"),772,"wn-airflowwcs",888);
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	public List<Map> getAlertListWeb(AlertData alertData, Object object) throws DBException {
		StringBuilder vpSQL = new StringBuilder();

        vpSQL.append("SELECT * FROM ALERTS WHERE 1=1 ");

        return fetchRecords(vpSQL.toString());
	}

	public List<Map> getAlertListDataWeb() throws DBException {
		StringBuilder vpSQL = new StringBuilder();

        vpSQL.append("SELECT * FROM ALERTS WHERE 1=1 ");

        return fetchRecords(vpSQL.toString());
	}

}
