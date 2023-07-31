package com.daifukuoc.wrxj.custom.ebs.util;

import com.daifukuamerica.wrxj.log.Logger;

/**
 * Thin wrapper for {@link com.daifukuamerica.wrxj.log.Logger}
 * @author Administrator
 *
 */
public class EBSLogger {
    private Logger logger;
    
    public EBSLogger(Logger logger) {
        this.logger = logger;
    }
    
    public String logDebug(String template, Object ... args) {
        String logMessage = String.format(template, args);
        logger.logDebug(logMessage);
        return logMessage;
    }   
    
    public String logError(String template, Object ... args) {
        String logMessage = String.format(template, args);
        logger.logError(logMessage);
        return logMessage;
    }
    
    public String logException(Exception ex) {
        logger.logException(ex);
        return ex.getMessage();
    }

}
