package org.marid.proto.health;

import org.marid.proto.ProtoBus;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoBusHealthMonitor implements AutoCloseable {

    private final ScheduledFuture<?> task;
    private final ScheduledExecutorService scheduler;

    public StdProtoBusHealthMonitor(ProtoBus bus, StdProtoBusHealthMonitorProps props) {
        final long timeout = props.getMaxRecencySeconds() * 1000L;
        final Runnable resetStrategy = () -> {
            final long timestamp = bus.getHealth().getLastSuccessfulTransactionTimestamp().getTime();
            final long now = System.currentTimeMillis();
            if (now - timestamp > timeout) {
                bus.getHealth().reset();
                bus.reset();
            }
        };
        final ScheduledExecutorService scheduler;
        if (props.getScheduler() == null) {
            scheduler = this.scheduler = new ScheduledThreadPoolExecutor(1);
        } else {
            scheduler = props.getScheduler();
            this.scheduler = null;
        }
        task = scheduler.scheduleWithFixedDelay(resetStrategy, props.getDelaySeconds(), props.getPeriodSeconds(), SECONDS);
    }

    @Override
    public void close() throws Exception {
        if (task != null) {
            task.cancel(false);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
