/*-
 * #%L
 * marid-proto
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.proto.impl.health;

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
