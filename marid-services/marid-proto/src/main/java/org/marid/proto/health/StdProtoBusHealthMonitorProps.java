package org.marid.proto.health;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoBusHealthMonitorProps {

    private long delaySeconds = 60L;
    private long periodSeconds = 60L;
    private long maxRecencySeconds = 180L;
    private ScheduledExecutorService scheduler;

    public long getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(long delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    public long getPeriodSeconds() {
        return periodSeconds;
    }

    public void setPeriodSeconds(long periodSeconds) {
        this.periodSeconds = periodSeconds;
    }

    public long getMaxRecencySeconds() {
        return maxRecencySeconds;
    }

    public void setMaxRecencySeconds(long maxRecencySeconds) {
        this.maxRecencySeconds = maxRecencySeconds;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }
}
