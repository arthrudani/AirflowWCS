package com.daifukuoc.wrxj.custom.ebs.communication;

import java.time.LocalDateTime;

/**
 * This is to check any timeout. 
 * - started() should be called when timeout should start up 
 * - ticked() should be called whenever the expected event happens
 * 
 * 2 cases of timeout are detected
 * - When ticked() wasn't called for the given timeout since started() was called 
 * - When ticked() wasn't called for the given timeout since the last time when ticked() was called
 * 
 * @author LK
 *
 */
public class TimeoutChecker {
    private long timeoutInSeconds = 0;
    private LocalDateTime started = null;
    private LocalDateTime ticked = null;

    public void started(long timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
        started = LocalDateTime.now();
        ticked = null;
    }

    public void ticked() {
        ticked = LocalDateTime.now();
    }

    public boolean check() {
        // If ticked() is not called, just return true always
        if (timeoutInSeconds == 0 || started == null) {
            return true;
        }

        if (ticked == null) {
            // If ticked() was not called yet, check timeout from when started() was called
            LocalDateTime timeout = started.plusSeconds(timeoutInSeconds);
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(timeout)) {
                return true;
            }
        } else {
            // Otherwise, check time from the previous execution of ticked()
            LocalDateTime timeout = ticked.plusSeconds(timeoutInSeconds);
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(timeout)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "TimeoutChecker [timeoutInSeconds=" + timeoutInSeconds + ", started=" + started
                + ", ticked=" + ticked + "]";
    }
}
