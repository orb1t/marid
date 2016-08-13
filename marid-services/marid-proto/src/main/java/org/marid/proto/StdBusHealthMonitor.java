/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.proto;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.marid.misc.Casts.pDur;
import static org.marid.misc.Casts.pLong;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdBusHealthMonitor implements AutoCloseable {

    private final ScheduledFuture<?> task;

    public StdBusHealthMonitor(ProtoBus bus, ScheduledExecutorService scheduler, Properties properties) {
        final long delay = pLong(properties, "delay", 10_000L);
        final long period = pLong(properties, "period", 10_000L);
        final Runnable resetStrategy;
        if (properties.containsKey("maxRecency")) {
            final Duration duration = pDur(properties, "maxRecency", Duration.ZERO);
            if (duration.toMillis() > 0) {
                resetStrategy = () -> {
                    final long timestamp = bus.getHealth().getLastSuccessfulTransactionTimestamp().getTime();
                    final long now = System.currentTimeMillis();
                    if (now - timestamp > duration.toMillis()) {
                        bus.getHealth().reset();
                        bus.reset();
                    }
                };
            } else {
                resetStrategy = null;
            }
        } else {
            resetStrategy = null;
        }
        if (resetStrategy != null) {
            task = scheduler.scheduleWithFixedDelay(resetStrategy, delay, period, MILLISECONDS);
        } else {
            task = null;
        }
    }

    @Override
    public void close() throws Exception {
        if (task != null) {
            task.cancel(false);
        }
    }
}
