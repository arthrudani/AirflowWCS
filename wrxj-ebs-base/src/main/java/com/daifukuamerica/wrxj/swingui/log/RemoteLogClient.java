package com.daifukuamerica.wrxj.swingui.log;

import com.daifukuamerica.wrxj.device.gateway.SystemGateway;
import com.daifukuamerica.wrxj.device.gateway.ThreadSystemGateway;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuamerica.wrxj.log.LoggingDataAccess;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import java.util.StringTokenizer;

/**
 * RemoteLogClient is a View into a remote logger that is in another running
 * instance of WRx-J. The RemoteLogClient accepts the data requests from the
 * log view/table and requests that data (and additional data determined by the
 * size of the local cache blocks) from the remote LogServer by publishing the
 * appropriate Log Events.
 *
 * @author Stephen Kendorski
 * @version 1.0
 */
public final class RemoteLogClient implements LoggingDataAccess
{
  /**
   * The Logging implementation for this named subsystem to use.
   */
  private Logger logger;
  /**
   * The name of the logs we are caching/viewing.
   */
  private String dataName = null;
  /**
   * An array containing the column numbers of displayed columns mapped to the
   * entries' data field indexes in the internal data Model.
   */
  private int[] dataMap = null;

  /**
   * The number of log entries currently available.
   */
  private int entryCount = 0;
  /**
   * Entry number of the next log entry to be added.
   */
  private int logSinkEntryNumber = 0;
  /**
   * A flag that is set when new data are added to the cache.
   */
  private boolean cacheChanges = false;
  /**
   * Number of blocks of cached data rows that can be retrieved from the remote
   * log server.
   */
  private int totalCacheBlocks = 0x10;
  /**
   * A bit mask used to get the high-order address (entry number) of an
   * individual block of cached data rows.
   */
  private int tagMask = 0;
  /**
   * A bit mask used to get the low-order address (entry number) of data rows
   * within an individual block of cached data rows.
   */
  private int cacheBlockMask = 0;
  /**
   * Size of a block of cached data accessed by its entries high-order data tag
   * address (entry numbers).
   */
  private int cacheBlockSize = 0x100;
  /**
   * Array of high-order cached data tag adresses (entry numbers) whose index is
   * used to find the actual cached data in the array "cacheBlocks[]".
   */
  private int[] tags = null;
  /**
   * Array of the actual cached data whose index matches the index of the array
   * of high-order cached data tag adresses (entry numbers) "tags[]".
   */
  private Object[] cacheBlocks = null;
  /**
   * Index value to use for the next block of new cached data rows that we need
   * to get from the remote log server. Has a maximum value of
   * totalCacheBlocks-1.
   */
  private int nextTagIndex = 0;
  /**
   * The last data row that the actual log viewer requested.
   */
  private Object[] lastRequestedRow = null;
  /**
   * The high-order address (entry number) tag of the last block of data
   * requested from the remote log server.
   */
  private int lastRequestedRemoteRowTag = -1;
  /**
   * The high-order address (entry number) tag of the last block of data
   * received from the remote log server.
   */
  private int lastRemoteRowTagRead = -1;
  /**
   * The greatest log entry (sequence) number received from the remote log
   * server.
   */
  private int latestLogEntryNumber = 0;

  private boolean filterActive = false;
  private String filterText = null;
  private int filterIndex = -1;
  
  private SystemGateway getSystemGateway()
  {
    return ThreadSystemGateway.get();
  }

  public void cleanup()
  {
    dataName = null;
    cacheBlocks = null;
    lastRequestedRow = null;
    filterText = null;
    logger = null;
  }
  
  /*------------------------------------------------------------------------*/
  /**
   * Creates new RemoteLogClient. When all cache blocks are used and a new tag
   * block is needed the earliest cache block is invalidated and used for the
   * new tag's data rows.
   * 
   * @param isDataName The name of the logs we are caching/viewing.
   * @param isLogServerName The name of the running instance of WRx-J whose
   *            log's we are viewing.
   */
  public RemoteLogClient(String isDataName, String isLogServerName, String isNameSpace)
  {
    //
    // This constructor is being called from within the SystemGateway's
    // Thread.  But we need the parent Frame's NameSpace.
    //
    logger = Logger.getLogger(isNameSpace);
    dataName = isDataName;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Initializes cache blocks. The cacheBlockSize should be a binary value that
   * can be and-ed with masks to obtain a high-order address to use for the tag
   * address of a cache block and a low-order index to find an address/entry
   * number within the cache block.
   * 
   * @param cacheBlockCount The maximum number of cache blocks to use.
   * @param inCacheBlockSize The number of data rows in a block of cached data
   *            that maps to to the high-order address (entry number) tag.
   */
  public void initializeCacheBlocks(int cacheBlockCount, int inCacheBlockSize)
  {
    //
    // Create the arrays of tags and cacheBlocks.
    //
    String s = Integer.toBinaryString(inCacheBlockSize - 1);
    if (s.indexOf("0") != -1)
    {
      logger.logError("CacheBlockSize: 0x"
          + Integer.toHexString(inCacheBlockSize) + " - Mask \"" + s
          + "\" - NOT a valid mask! - RemoteLogClient.initializeCacheBlocks");
      inCacheBlockSize = 0x100; // use default.
    }
    totalCacheBlocks = cacheBlockCount;
    cacheBlockSize = inCacheBlockSize;
    tagMask = (~(inCacheBlockSize - 1));
    cacheBlockMask = (inCacheBlockSize - 1);
    tags = new int[cacheBlockCount];
    cacheBlocks = new Object[cacheBlockCount];
    nextTagIndex = 0;
    //
    // Create the cache blocks and invalidate their contents.
    //
    for (int i = 0; i < cacheBlockCount; i ++)
    {
      //
      // Show no data tags exist.
      //
      tags[i] = -1;
      Object[] cacheBlock = new Object[inCacheBlockSize];
      invalidateCacheBlock(cacheBlock);
      cacheBlocks[i] = cacheBlock;
    }
  }

  /*------------------------------------------------------------------------*/
  public boolean getFilterActive()
  {
    return filterActive;
  }
  
  /*------------------------------------------------------------------------*/
  public String getFilterText()
  {
    return filterText;
  }
  
  /*------------------------------------------------------------------------*/
  public int getFilterIndex()
  {
    return filterIndex;
  }
  /*------------------------------------------------------------------------*/
  /**
   * Specify an array containing the map of displayed columns to the data
   * fields in a Model.
   *
   * @param ianDataMap the ordered array
   */
  public void setColumnToFieldMap(int[] ianDataMap)
  {
    dataMap = ianDataMap;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Fetch the name of the logs we are caching/viewing.
   *
   * @return the name.
   */
  public String getDataName()
  {
    return dataName;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Return the number of log entries currently available.
   *
   * @return number of log entries
   */
  public int getEntryCount()
  {
    if (filterActive)
    {
      //
      // The filter is no longer active.
      //
      initializeFilter("", filterIndex);
      return 0; 
    }
    return pGetEntryCount();
  }

  /**
   * Return the number of log entries currently available.
   *
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @return number of log entries
   */
  public int getEntryCount(String isFilterText, int inFilterIndex)
  {
    if ((!isFilterText.equals(filterText)) ||
        (inFilterIndex != filterIndex))
    {
      //
      // The filter parameters have changed.
      //
      initializeFilter(isFilterText, inFilterIndex); 
    }
    return pGetEntryCount();
  }

  /**
   * Get the entry count
   * @return
   */
  private int pGetEntryCount()
  {
    return entryCount;
  }

  /**
   * Set the entry count
   * @param inEntryCount
   */
  public void setEntryCount(int inEntryCount)
  {
    if (filterActive)
    {
      //
      // The filter is no longer active.
      //
      initializeFilter("", filterIndex);
    }
    pSetEntryCount(inEntryCount);
  }

  /**
   * Set the entry count
   * @param inEntryCount
   * @param isFilterText
   * @param inFilterIndex
   */
  public void setEntryCount(int inEntryCount, String isFilterText,
      int inFilterIndex)
  {
    if ((!isFilterText.equals(filterText)) || (inFilterIndex != filterIndex))
    {
      //
      // The filter parameters have changed.
      //
      initializeFilter(isFilterText, inFilterIndex); 
    }
    pSetEntryCount(inEntryCount);
  }

  /**
   * Set the entry count
   * @param inEntryCount
   */
  private void pSetEntryCount(int inEntryCount)
  {
    entryCount = inEntryCount;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Fetch the number of the earliest entry that is still in the logger. This
   * number can be greater than the logger buffer capacity.
   * 
   * @return first available log sequence number.
   */
  public int getEarliestLogEntryNumber()
  {
    return 0;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Get the maximum number of log entries (minimum 1)
   * @return int
   */
  public int getMaxLogEntries()
  {
    int result = entryCount;
    if (entryCount == 0)
    {
      result = 1;
    }
    return result;
  }
  /*--------------------------------------------------------------------------*/
  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @return the offset
   */
  public int getLogSinkEntryNumber()
  {
    if (filterActive)
    {
      //
      // The filter is no longer active.
      //
      initializeFilter("", filterIndex);
    }
    return logSinkEntryNumber;
  }

  /**
   * Fetch the offset to where the next log record will be saved (the "sink").
   *
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @return the offset
   */
  public int getLogSinkEntryNumber(String isFilterText, int inFilterIndex)
  {
    return logSinkEntryNumber;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Specify the offset to where the next log record will be saved (the "sink").
   *
   * @param inLogSinkEntryNumber the offset
   */
  public void setLogSinkEntryNumber(int inLogSinkEntryNumber)
  {
    if (filterActive)
    {
      //
      // The filter is no longer active.
      //
      initializeFilter("", filterIndex);
    }
    pSetLogSinkEntryNumber(inLogSinkEntryNumber);
  }

  /**
   * Specify the offset to where the next log record will be saved (the "sink").
   *
   * @param inLogSinkEntryNumber the offset
   * @param isFilterText pattern match to apply to records
   * @param isFilterIndex the offset to the field within the record
   */
  public void setLogSinkEntryNumber(int inLogSinkEntryNumber,
      String isFilterText, int isFilterIndex)
  {
    if ((!isFilterText.equals(filterText)) || (isFilterIndex != filterIndex))
    {
      //
      // The filter parameters have changed.
      //
      initializeFilter(isFilterText, isFilterIndex); 
    }
    pSetLogSinkEntryNumber(inLogSinkEntryNumber);
  }

  /**
   * Set the log sink entry number
   * @param inLogSinkEntryNumber
   */
  private void pSetLogSinkEntryNumber(int inLogSinkEntryNumber)
  {
    if (inLogSinkEntryNumber != logSinkEntryNumber)
    {
      int logSinkEntryNumberTag = inLogSinkEntryNumber & tagMask; // Get high-order index.
      if (logSinkEntryNumberTag == lastRequestedRemoteRowTag)
      {
        //
        // Make sure we add the new rows to our cache.
        //
        lastRequestedRemoteRowTag = -1;
      }
      if (logSinkEntryNumber > inLogSinkEntryNumber)
      {
        //
        // Our logSink has wrapped.  Invalidate the cache that we have wrapped
        // over.
        //
        invalidateCache(logSinkEntryNumber, entryCount - 1);
        invalidateCache(0, inLogSinkEntryNumber - 1);
      }
      else
      {
        invalidateCache(logSinkEntryNumber, inLogSinkEntryNumber - 1);
      }
      cacheChanges = true;
      logSinkEntryNumber = inLogSinkEntryNumber;
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * Return the sequence number of the most recent available log.
   *
   * @return most recent available log sequence number.
   */
  public int getLatestLogEntryNumber()
  {
    if (filterActive)
    {
      //
      // The filter is no longer active.
      //
      initializeFilter("", filterIndex);
    }
    return entryCount;
  }

  /**
   * Return the sequence number of the most recent available log.
   *
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @return most recent available log sequence number.
   */
  public int getLatestLogEntryNumber(String isFilterText, int inFilterIndex)
  {
    return entryCount;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Set the sequence number of the most recent remote available log.
   *
   * @param entryNumber most recent available log sequence number.
   */
  public void setLatestLogEntryNumber(int entryNumber)
  {
    if (filterActive)
    {
      //
      // The filter is no longer active.
      //
      initializeFilter("", filterIndex);
    }
    pSetLatestLogEntryNumber(entryNumber);
  }

  /**
   * Set the sequence number of the most recent remote available log.
   *
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @param inEntryNumber most recent available log sequence number.
   */
  public void setLatestLogEntryNumber(int inEntryNumber, String isFilterText,
      int inFilterIndex)
  {
    if ((!isFilterText.equals(filterText)) || (inFilterIndex != filterIndex))
    {
      //
      // The filter parameters have changed.
      //
      initializeFilter(isFilterText, inFilterIndex); 
    }
    pSetLatestLogEntryNumber(inEntryNumber);
  }

  /**
   * Set the latest log entry number
   * @param entryNumber
   */
  private void pSetLatestLogEntryNumber(int entryNumber)
  {
    if (entryNumber > (latestLogEntryNumber + entryCount))
    {
      //
      // If we had anything cached it's no longer valid.  Invalidate
      // all the cache.
      //
      for (int i = 0; i < totalCacheBlocks; i ++)
      {
        if (tags[i] != -1)
        {
          //
          // We have a valid tag for a cache vlock - invalidate the cache and the tag.
          //
          invalidateCacheBlock((Object[])cacheBlocks[i]);
          tags[i] = -1;
        }
      }
      //
      // Now that all cached data is invalidated, reset our indexes.
      //
      nextTagIndex = 0;
    }
    latestLogEntryNumber = entryNumber;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Return <i>true</i> if additional logs exist. If the current logging
   * sequence number is different than that in the caller's passed in parameter
   * return <i>true</i>, otherwise return <i>false</i>.
   * 
   * @param callersLatestLogEntryNumber most recent log sequence number known
   * @return result of additional logs
   */
  public boolean newLogsAvailable(int callersLatestLogEntryNumber)
  {
    boolean result = ((cacheChanges) ||
                      (entryCount != callersLatestLogEntryNumber));
    cacheChanges = false;
    return result;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset. If the caller's parameter "bAll" is false,
   * return a truncated String if the String length exceeds a pre-determined
   * length.
   * 
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @return the Object at entryNumber[field]
   */
  public Object getValue(int entryNumber, int index, boolean bAll)
  {
    Object result = null;
    Object[] cachedRow = getCachedRow(entryNumber);
    if (cachedRow != null)
    {
      result = cachedRow[index];
    }
    else
    {
      if (index != LogConsts.LOG_ENTRY_CTLRKEY_IDX)
      {
        result = "";
      }
      else
      {
        Integer i = Integer.valueOf(0);
        result = i;
      }
    }
    return result;
  }

  /**
   * Fetch the data from the specified index/field within the record at the
   * specified entryNumber/offset. If the caller's parameter "bAll" is false,
   * return a truncated String if the String length exceeds a pre-determined
   * length.
   * 
   * @param entryNumber the data record/row offset
   * @param index the offset to the field within the record
   * @param bAll if true, return untruncated String
   * @param isFilterText pattern match to apply to records
   * @param inFilterIndex the offset to the field within the record
   * @return the Object at entryNumber[field]
   */
  public Object getValue(int entryNumber, int index, boolean bAll,
      String isFilterText, int inFilterIndex)
  {
    return getValue(entryNumber, index, bAll);
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Return the number of the log entry that contains the search text, -1 if
   * search text not found, -2 if do nothing.
   * 
   * @param findText pattern match to apply to records
   * @param startEntry number of entry where search begins
   * @param findIndex the offset to the field within the record
   * @param down if true, search by descending log entries
   * @return number of log entry that has data match
   */
  public int findText(String findText, int startEntry, int findIndex,
      boolean down)
  {
    String vsDown = down?"DOWN" : "UP";
    String vsText = dataName + "\t" + findText + "\t" + startEntry + "\t"
        + findIndex + "\t" + vsDown + "\t";
    publishLogEvent(vsText + "Find Text", LogConsts.FIND_TEXT, 
        SKDCConstants.LOG_SERVER);
    //
    // -2 says not found, do NOT display dialog.
    //
    return -2;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /**
   * Process a batch of logs sent by a remote Log Server.
   *
   * @param dataLines the data rows in a delimited String.
   */
  public void readLogs(String dataLines)
  {
    StringTokenizer stringTokenizer = new StringTokenizer(dataLines, "\n");
    String vsEntryIndex = stringTokenizer.nextToken();
    Integer viEntryIndexInteger = Integer.valueOf(vsEntryIndex);
    int cacheBlockIndex = viEntryIndexInteger.intValue();
    int newDataTag = cacheBlockIndex & tagMask; // Get high-order index.
    cacheBlockIndex = cacheBlockIndex & cacheBlockMask;
    int dataTag = -1;
    Object[] cacheBlock = (Object[])cacheBlocks[nextTagIndex];//null;
    for (int i = 0; i < totalCacheBlocks; i++)
    {
      if (tags[i] == newDataTag)
      {
        dataTag = newDataTag;
        cacheBlock = (Object[])cacheBlocks[i];
        break;
      }
    }
    if (dataTag == -1)
    {
      //
      // This block not yet cached.
      //
      dataTag = newDataTag;
      cacheBlock = (Object[])cacheBlocks[nextTagIndex];
      tags[nextTagIndex] = newDataTag;
      invalidateCacheBlock(cacheBlock);
      nextTagIndex++;
      if (nextTagIndex >= totalCacheBlocks)
      {
        nextTagIndex = 0;
      }
    }
    String sLine = null;
    while (stringTokenizer.hasMoreTokens())
    {
      sLine = stringTokenizer.nextToken();
      if(cacheBlock[cacheBlockIndex] == null)
      {
        StringTokenizer st = new StringTokenizer(sLine, "\t");
        int cc = 0;
        Object[] dataLineFields = new Object[LogConsts.LOG_ENTRY_MAX_FIELDS];
        while (st.hasMoreTokens())
        {
          String sField = st.nextToken();
          switch (dataMap[cc])
          {
            case LogConsts.LOG_ENTRY_NUMBER_IDX:
              dataLineFields[dataMap[cc]] = sField;
              break;
            case LogConsts.LOG_ENTRY_CTLRKEY_IDX:
              dataLineFields[dataMap[cc]] = Integer.valueOf(sField);
              break;
            default:
              if (sField.indexOf(0x01) != -1)
              {
                sField = sField.replace((char)0x01, (char)0x0d);
              }
              if (sField.indexOf(0x02) != -1)
              {
                sField = sField.replace((char)0x02, (char)0x0a);
              }
              if (sField.indexOf(0x03) != -1)
              {
                sField = sField.replace((char)0x03, (char)0x09);
              }
              dataLineFields[dataMap[cc]] = sField;
              break;
          }
          cc++;
        }
        //
        // This new row is NOT already in the cache - add it.
        //
        cacheBlock[cacheBlockIndex] = dataLineFields;
      }
      cacheBlockIndex++;
    }
    lastRemoteRowTagRead = dataTag;
    cacheChanges = true;
  }

  /*------------------------------------------------------------------------*/
  /**
   * Invalidate all cached data rows and tags.
   */
  private void invalidateCache()
  {
    for (int i = 0; i < totalCacheBlocks; i ++)
    {
      //
      // Show no data tags exist.
      //
      tags[i] = -1;
      Object[] cacheBlock = (Object[])cacheBlocks[i];
      invalidateCacheBlock(cacheBlock);
    }
  }
  /*------------------------------------------------------------------------*/
  /**
   * Invalidate a block of cached data rows.
   *
   * @param cacheBlock the cached block to invalidate..
   */
  private void invalidateCacheBlock(Object[] cacheBlock)
  {
    for (int i = 0; i < cacheBlockSize; i++)
    {
      cacheBlock[i] = null;
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * Invalidate a range of data rows in the cached data rows (range may cover
   * more than one cache block).
   *
   * @param startRowIndex address (entry number) of start of block.
   * @param endRowIndex address (entry number) of end of block.
   */
  private void invalidateCache(int startRowIndex, int endRowIndex)
  {
    boolean cacheInvalidated = false;
    int endRowTag = endRowIndex & tagMask; // Get high-order index.
    int endRowCacheBlockIndex = endRowIndex & cacheBlockMask; // Low-order.
    //
    while (!cacheInvalidated)
    {
      int startRowTag = startRowIndex & tagMask; // Get high-order index.
      int startRowCacheBlockIndex = startRowIndex & cacheBlockMask; // Low-order.
      int cacheBlockEndIndex = endRowCacheBlockIndex;
      if (startRowTag != endRowTag)
      {
        //
        // All rows are NOT in the same cache block.
        //
        cacheBlockEndIndex = cacheBlockSize - 1;
      }
      cacheInvalidated = invalidateCache(startRowCacheBlockIndex,
          cacheBlockEndIndex, startRowTag);
      startRowIndex = startRowTag + cacheBlockSize;
    }
  }

  /*------------------------------------------------------------------------*/
  /**
   * Invalidate a range of data rows within an individual block of cached data
   * rows by setting the entries to null.
   * 
   * @param startRowCacheBlockIndex low-order address (entry number) of start of
   *            block.
   * @param endRowCacheBlockIndex low-order address (entry number) of end of
   *            block.
   * @param cacheTag high-order address (entry number) cache block.
   * 
   * @return true if invalidated cache row encountered; otherwise return false.
   */
  private boolean invalidateCache(int startRowCacheBlockIndex,
      int endRowCacheBlockIndex, int cacheTag)
  {
    boolean done = false;
    Object[] cachedBlock = null;
    for (int i = 0; i < totalCacheBlocks; i++)
    {
      if (tags[i] == cacheTag)
      {
        cachedBlock = (Object[])cacheBlocks[i];
        break;
      }
    }
    if (cachedBlock != null)
    {
      for (int j = startRowCacheBlockIndex; j <= endRowCacheBlockIndex; j++)
      {
        if (cachedBlock[j] == null)
        {
          done = true;
          break;
        }
        cachedBlock[j] = null;
      }
    }
    else
    {
      done = true;
    }
    return done;
  }

  /*-------------------------------------------------------------------------*/
  /*-------------------------------------------------------------------------*/
  /**
   * Fetches a particular data row in the cached data.  If the data row is not
   * in the cache block we request the required data rows from the remote Log
   * Server and return null.
   *
   * @param requestedRowIndex the entry number of the row to fetch.
   *
   * @return the requested data row, or null if entry not cached.
   */
  private Object[] getCachedRow(int requestedRowIndex)
  {
    //
    int requestedRowTag = requestedRowIndex & tagMask; // Get high-order index.
    //
    // Find our high-order tag address/entry number.
    //
    int requestedRowCacheBlockIndex = requestedRowIndex & cacheBlockMask;
    lastRequestedRow = null;
    Object[] cachedBlock = null;
    //
    // Search through the tag array to see if this data row's tag is cached.
    //
    for (int i = 0; i < totalCacheBlocks; i++)
    {
      if (tags[i] == requestedRowTag)
      {
        //
        // The data row's tag cache block exists, see if the requested data row
        // is actually in the cache block.
        //
        cachedBlock = (Object[])cacheBlocks[i];
        lastRequestedRow = (Object[])cachedBlock[requestedRowCacheBlockIndex];
        break;
      }
    }
    if (lastRequestedRow == null)
    {
      //
      // Requested row is NOT in cache - get it.
      //
      if ((requestedRowTag != lastRequestedRemoteRowTag) ||
          (requestedRowTag == lastRemoteRowTagRead))
      {
        //
        // Get the remote block if we are requesting a new block, or the row
        // in our cached block has not yet been read from the remote logs.
        //
        lastRequestedRemoteRowTag = requestedRowTag;
        if ((cachedBlock == null) ||
            (cachedBlock[0] == null))
        {
          //
          // We don't have the start of the block - get it.
          //
          requestedRowIndex = requestedRowTag;
        }
        else
        {
          //
          // Make sure we get the earliest available entry.
          //
          for (int i = 0; i < requestedRowCacheBlockIndex; i++)
          {
            if (cachedBlock[i] == null)
            {
              requestedRowIndex = requestedRowTag + i;
              break;
            }
          }
        }
        lastRemoteRowTagRead = -1;
        getBlock(requestedRowIndex);
      }
    }
    return lastRequestedRow;
  }

  /*-------------------------------------------------------------------------*/
  /**
   * Request a block of data rows from the remote Log Server.  The requested
   * entry row range is from the caller's requested entry number to the end of
   * it's tag block (high-order address/entry number).
   *
   * @param requestedRemoteRow first row in requested block.
   */
  private void getBlock(int requestedRemoteRow)
  {
    int endRow = lastRequestedRemoteRowTag + (cacheBlockSize -1);
    if (!filterActive)
    {
      String vsText = dataName + "\t" + requestedRemoteRow + "\t" + endRow + "\t";
      publishLogEvent(vsText + "Get Log Entries", LogConsts.SEND_LOG_ENTRIES,
          SKDCConstants.LOG_SERVER);
    }
    else
    {
      String vsText = dataName + "\t" + requestedRemoteRow + "\t" + endRow
          + "\t" + "\t" + filterText + "\t" + filterIndex + "\t";
      publishLogEvent(vsText + "Get FILTERED Log Entries",
          LogConsts.SEND_FILTERED_LOG_ENTRIES, SKDCConstants.LOG_SERVER);
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Initialize the filter
   */
  private void initializeFilter(String isFilterText, int inFilterIndex)
  {
    filterText = isFilterText;
    filterIndex = inFilterIndex;
    invalidateCache();
    if (isFilterText.length() == 0)
    {
      filterActive = false;
      return;
    }
    filterActive = true;
    String vsText = dataName + "\t" + isFilterText + "\t" + inFilterIndex + "\t";
    publishLogEvent(vsText + "\tGet FILTERED Log Entry Count",
        LogConsts.SEND_FILTERED_LOG_ENTRY_COUNT, SKDCConstants.LOG_SERVER);
  }
  
  /*--------------------------------------------------------------------------*/
  /**
   * Publish a "Log" Message to the Inter-Process-Communication message
   * service <i>Server</i>.
   *
   * @param sEvent the String data content to be sent
   * @param iEvent the int data content to be sent
   * @param sCKN the message destination
   */
  private void publishLogEvent(String sEvent, int iEvent, String sCKN)
  {
    getSystemGateway().publishLogEvent(sEvent, iEvent, sCKN);
  }

@Override
public Object getValueSetDisplayFormat(int entryNumber, int index,
		boolean bAll, int displayFormat) {
	// TODO Auto-generated method stub
	return null;
}

@Override
public String getLogName() {
	// TODO Auto-generated method stub
	return null;
}
}
