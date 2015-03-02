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

package org.marid.service.proto.model;

import org.marid.service.proto.util.MapUtil;
import org.marid.service.proto.util.NestedMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProtoContext extends AbstractProtoObject implements ProtoTimerSupport {

    protected final TreeMap<String, ProtoBus> busMap = new TreeMap<>();
    protected final ScheduledThreadPoolExecutor timer;

    public ProtoContext(@Nonnull Map<String, Object> map) {
        super(map.get("name"), new NestedMap(), map);
        MapUtil.children(map, "buses").forEach((k, v) -> busMap.put(MapUtil.name(k), new ProtoBus(this, k, v)));
        ProtoTimerSupport.putProperties(map, this);
        timer = new ScheduledThreadPoolExecutor(getThreads());
    }

    @Override
    public AbstractProtoObject getParent() {
        return null;
    }

    @Override
    public ProtoContext getContext() {
        return this;
    }

    @Override
    public void start() {
        busMap.values().forEach(AbstractProtoObject::start);
    }

    @Override
    public void stop() {
        busMap.values().forEach(AbstractProtoObject::stop);
    }

    @Override
    public boolean isRunning() {
        return busMap.values().stream().anyMatch(AbstractProtoObject::isRunning);
    }

    @Override
    public boolean isStarted() {
        return busMap.values().stream().allMatch(AbstractProtoObject::isStarted);
    }

    @Override
    public boolean isStopped() {
        return busMap.values().stream().noneMatch(AbstractProtoObject::isRunning);
    }

    @Override
    public void close() throws Exception {
        stop();
        timer.shutdown();
        timer.awaitTermination(getShutdownTimeout(), TimeUnit.SECONDS);
    }
}
