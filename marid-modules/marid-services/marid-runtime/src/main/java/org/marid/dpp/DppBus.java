/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.dpp;

import org.marid.methods.PropMethods;
import org.marid.tree.StaticTreeObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.marid.methods.PropMethods.getRejectedExecutionHandler;
import static org.marid.methods.PropMethods.getThreadFactory;


/**
 * @author Dmitry Ovchinnikov
 */
public class DppBus extends StaticTreeObject {

    protected final ThreadGroup timerThreadGroup;
    protected final ThreadGroup executorThreadGroup;
    protected final ScheduledThreadPoolExecutor timer;
    protected final Map<String, ScheduledFuture<?>> taskMap = new HashMap<>();
    protected final boolean logDurations;

    public DppBus(DppScheduler parent, String name, Map params) {
        super(parent, name, params);
        logDurations = PropMethods.get(params, boolean.class, "logDurations", parent.logDurations);
        timerThreadGroup = new ThreadGroup("timer:" + label);
        executorThreadGroup = new ThreadGroup("executor:" + label);
        timer = new ScheduledThreadPoolExecutor(
                PropMethods.get(params, int.class, "timerThreadCount", 1),
                getThreadFactory(params, "timerThreadFactory", timerThreadGroup,
                        PropMethods.get(params, boolean.class, "timerDaemon", false),
                        PropMethods.get(params, int.class, "timerStackSize", 0)),
                getRejectedExecutionHandler(params, "timerRejectedExecutionHandler"));
        timer.setRemoveOnCancelPolicy(
                PropMethods.get(params, boolean.class, "timerRemoveOnCancel", true));
        DppUtil.addTasks(logger, this, children, params);
    }

    @Override
    public DppScheduler parent() {
        return (DppScheduler) super.parent();
    }

    public void start() {
        if (!timer.isShutdown()) {
            for (final StaticTreeObject child : children.values()) {
                if (child instanceof DppTask) {
                    ((DppTask) child).start();
                }
            }
        }
    }

    public void stop() {
        if (!timer.isShutdown()) {
            for (final StaticTreeObject child : children.values()) {
                if (child instanceof DppTask) {
                    ((DppTask) child).stop();
                }
            }
            timer.shutdown();
            children.clear();
        }
    }
}
