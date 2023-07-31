package com.daifukuoc.wrxj.custom.ebs.host.processor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.messages.MessageParserFactory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBRuntimeException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.AisleBasedEmptyLocationFinderImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.EmptyLocationFinder;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.DefaultFlightDataUpdaterImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.flightdataupdate.FlightDataUpdater;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryreqbyflight.InventoryReqByFlightImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryreqbyflight.InventoryReqByFlight;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate.InventoryUpdater;
import com.daifukuoc.wrxj.custom.ebs.host.processor.inventoryupdate.InventoryUpdaterImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalitemorder.AisleBasedFlightItemRetrieverImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalitemorder.LoadItemRetriever;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.AisleBasedFlightLoadRetrieverImpl;
import com.daifukuoc.wrxj.custom.ebs.host.processor.retrievalorder.LoadRetriever;

/**
 * Responsible for populating a processor object
 * 
 * @author LK
 *
 */
public class ProcessorFactory {
    private static HostConfig mpHostCfg;
    private static Map<String, Class<? extends Processor>> cache;
    private static Logger logger = Logger.getLogger();

    // Factory pattern, so use public static methods below
    private ProcessorFactory() {
    }

    /**
     * Load a processor as configured in HostConfig table Same with how {@link MessageParserFactory} loads a message
     * parser
     * 
     * @param controllerName sDataHandler in host config table, for example, "HostExpectedReceiptMessageHandler"
     * @param processorName sParameterName in host config table, for example, "EmptyLocationFinder"
     * @return Processor object loaded from the full class path, for example,
     *         "com.daifukuoc.wrxj.custom.ebs.host.processor.expectedreceipt.AisleBasedEmptyLocationFinderImpl"
     * @throws DBRuntimeException When failed to create a processor object
     */
    public static synchronized Processor get(String controllerName, String processorName) {
        if (controllerName == null || controllerName.isEmpty()) {
            throw new IllegalArgumentException("controllerName is null or empty");
        }
        if (processorName == null || processorName.isEmpty()) {
            throw new IllegalArgumentException("processorName is null or empty");
        }

        Processor processor = null;
        try {
            // Initialize if not created yet
            if (cache == null) {
                cache = new ConcurrentHashMap<>();
            }
            // If it's not found in the cache, create a new one
            if (!cache.containsKey(processorName)) {
                // Read processor class name from host config table
                mpHostCfg = Factory.create(HostConfig.class);
                String processorClassName = mpHostCfg.getProcessorClassName(controllerName, processorName);
                Class<? extends Processor> processorClass = Class.forName(processorClassName)
                        .asSubclass(Processor.class);
                cache.put(processorName, processorClass);
            }
            // Now we should have it in the cache
            Class<? extends Processor> cachedClass = cache.get(processorName);
            processor = cachedClass.newInstance();
            logger.logDebug("The configured " + processor.getClass().getName() + " is loaded");
        } catch (ClassNotFoundException e) {
            logger.logException(e);
        } catch (InstantiationException e) {
            logger.logException(e);
        } catch (IllegalAccessException e) {
            logger.logException(e);
        } catch (DBException e) {
            logger.logException(e);
        } catch (Exception e) {
            logger.logException(e);
        } finally {
            // If anything happens in loading a processor, fall back to the default implementation
            if (processor == null) {
                logger.logError("Failed to load a processor(" + processorName
                        + "), so falling back to the default implementation");
                if (processorName.equals(EmptyLocationFinder.NAME)) {
                    logger.logError("AisleBasedEmptyLocationFinderImpl is loaded for " + processorName);
                    processor = new AisleBasedEmptyLocationFinderImpl();
                }
                else if (processorName.equals(FlightDataUpdater.NAME)) {
                    logger.logError("DefaultFlightDataUpdaterImpl is loaded for " + processorName);
                    processor = new DefaultFlightDataUpdaterImpl();
                }
                else if (processorName.equals(InventoryUpdater.NAME)) {
                	logger.logError("InventoryUpdateImpl is loaded for " + processorName);
                	processor = new InventoryUpdaterImpl();
                }
                else if (processorName.equals(LoadRetriever.NAME)) {
                    logger.logError("AisleBasedFlightLoadRetrieverImpl is loaded for " + processorName);
                    processor = new AisleBasedFlightLoadRetrieverImpl();
                }
                else if (processorName.equals(InventoryReqByFlight.NAME)) {
                    logger.logError("InventoryByFlightImpl is loaded for " + processorName);
                    processor = new InventoryReqByFlightImpl();
                }
                else if (processorName.equals(LoadItemRetriever.NAME)) {
                    logger.logError("AisleBasedItemLoadRetrieverImpl is loaded for " + processorName);
                    processor = new AisleBasedFlightItemRetrieverImpl();
                }
               
            }
        }

        return processor;
    }
    
    /**
     * Removes all caches objects, mainly for testing purpose
     * 
     */
    public static synchronized void reset() {
        if (cache != null) {
            cache.clear();
        }
    }
}
