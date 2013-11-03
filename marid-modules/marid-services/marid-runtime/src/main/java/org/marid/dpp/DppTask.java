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

import org.marid.ParameterizedException;
import org.marid.groovy.ClosureChain;
import org.marid.methods.PropMethods;
import org.marid.tree.StaticTreeObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.marid.methods.LogMethods.finest;
import static org.marid.methods.LogMethods.info;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class DppTask extends StaticTreeObject implements Runnable {

    protected final ClosureChain func;
    protected final long delay;
    protected final long period;
    protected final TimeUnit timeUnit;
    protected final boolean fixedRate;
    protected final boolean interruptThread;
    protected final boolean logDurations;

    protected DppTask(StaticTreeObject parent, String name, Map params) {
        super(parent, name, params);
        func = new ClosureChain(DppUtil.func(funcs(params.get("func"))));
        delay = PropMethods.get(params, long.class, "delay", defDelay());
        period = PropMethods.get(params, long.class, "period", defPeriod());
        timeUnit = PropMethods.get(params, TimeUnit.class, "timeUnit", defaultTimeUnit());
        fixedRate = PropMethods.get(params, boolean.class, "fixedRate", false);
        interruptThread = PropMethods.get(params, boolean.class, "interruptThread", true);
        logDurations = PropMethods.get(params, boolean.class, "logDurations", defLogDurations());
    }

    private long defDelay() {
        return parent instanceof DppGroup ? ((DppGroup) parent).delay : -1L;
    }

    private long defPeriod() {
        return parent instanceof DppGroup ? ((DppGroup) parent).period : -1L;
    }

    private TimeUnit defaultTimeUnit() {
        return parent instanceof DppGroup ? ((DppGroup) parent).timeUnit : TimeUnit.SECONDS;
    }

    private boolean defLogDurations() {
        if (parent instanceof DppBus) {
            return ((DppBus) parent).logDurations;
        } else if (parent instanceof DppTask) {
            return ((DppTask) parent).logDurations;
        } else {
            return true;
        }
    }

    protected LinkedList<Object> funcs(Object f) {
        final LinkedList<Object> list = new LinkedList<>();
        if (f != null) {
            list.add(f);
        }
        for (StaticTreeObject o = parent; o instanceof DppGroup; o = o.parent()) {
            final DppGroup g = (DppGroup) o;
            if (g.prefixFunc != null) {
                list.addFirst(g.prefixFunc);
            }
            if (g.postfixFunc != null) {
                list.addLast(g.postfixFunc);
            }
        }
        return list;
    }

    public DppBus bus() {
        for (StaticTreeObject o = parent; o != null; o = o.parent()) {
            if (o instanceof DppBus) {
                return (DppBus) o;
            }
        }
        throw new IllegalStateException("No bus for " + this);
    }

    @Override
    public Object get(String name) {
        final Object value = super.get(name);
        if (value == null) {
            if (parent instanceof DppGroup) {
                return parent.get(name);
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    @Override
    public <T> T get(Class<T> type, String key) {
        final T value = super.get(type, key);
        if (value == null) {
            if (parent instanceof DppGroup) {
                return parent.get(type, key);
            } else {
                return value;
            }
        } else {
            return value;
        }
    }

    @Override
    public void run() {
        final long startTime = logDurations ? System.nanoTime() : 0L;
        try {
            final Object o = func.call(logger, this, new HashMap<>());
            finest(logger, "Result: {0}", o);
        } catch (ParameterizedException x) {
            if (x.getCause() != null) {
                warning(logger, x.getMessage(), x.getCause(), x.getArgs());
            } else {
                warning(logger, x.getMessage(), x.getArgs());
            }
        } catch (Exception x) {
            warning(logger, "Run error", x);
        } finally {
            if (logDurations) {
                info(logger, "Executed in {0} s", (System.nanoTime() - startTime) * 1e-9);
            }
        }
    }

    public void start() {
        final DppBus bus = bus();
        synchronized (bus.taskMap) {
            if (!bus.taskMap.containsKey(label)) {
                final ScheduledFuture<?> f;
                if (period <= 0L) {
                    f = bus.timer.schedule(this, delay < 0L ? 0L : delay, timeUnit);
                } else {
                    final long d = delay < 0L ? 0L : delay;
                    if (fixedRate) {
                        f = bus.timer.scheduleAtFixedRate(this, d, period, timeUnit);
                    } else {
                        f = bus.timer.scheduleWithFixedDelay(this, d, period, timeUnit);
                    }
                }
                bus.taskMap.put(label, f);
                return;
            }
        }
        warning(logger, "Already started");
    }

    public void stop() {
        final DppBus bus = bus();
        synchronized (bus.taskMap) {
            final ScheduledFuture<?> f = bus.taskMap.remove(label);
            if (f != null) {
                f.cancel(interruptThread);
                return;
            }
        }
        warning(logger, "Already stopped");
    }
}
