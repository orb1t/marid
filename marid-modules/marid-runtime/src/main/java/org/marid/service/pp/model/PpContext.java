/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.service.pp.model;

import org.marid.service.pp.util.MapUtil;
import org.marid.service.pp.util.NestedMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public class PpContext extends AbstractPpObject {

    protected final TreeMap<String, PpBus> busMap = new TreeMap<>();
    protected final ScheduledThreadPoolExecutor timer;

    public PpContext(String name, @Nonnull Map<String, Object> map) {
        super(name, new NestedMap(), map);
        MapUtil.children(map, "buses").forEach((k, v) -> busMap.put(MapUtil.name(k), new PpBus(this, k, v)));
        putProperty(map, "threads", int.class);
        putProperty(map, "shutdownTimeout", long.class);
        timer = new ScheduledThreadPoolExecutor(getThreads());
    }

    @Override
    public AbstractPpObject getParent() {
        return null;
    }

    @Override
    public PpContext getContext() {
        return this;
    }

    @Override
    public void start() {
        busMap.values().forEach(AbstractPpObject::start);
    }

    @Override
    public void stop() {
        busMap.values().forEach(AbstractPpObject::stop);
    }

    @Override
    public boolean isRunning() {
        return busMap.values().stream().anyMatch(AbstractPpObject::isRunning);
    }

    @Override
    public boolean isStarted() {
        return busMap.values().stream().allMatch(AbstractPpObject::isStarted);
    }

    @Override
    public boolean isStopped() {
        return busMap.values().stream().noneMatch(AbstractPpObject::isRunning);
    }

    public int getThreads() {
        return getProperty("threads", () -> 1);
    }

    public long getShutdownTimeout() {
        return getProperty("shutdownTimeout", () -> 60L);
    }

    @Override
    public void close() throws Exception {
        for (final PpBus protoBus : busMap.values()) {
            protoBus.close();
        }
        timer.shutdown();
        timer.awaitTermination(getShutdownTimeout(), TimeUnit.SECONDS);
    }
}
