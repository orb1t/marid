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

package org.marid.service.proto.pp;

import org.marid.service.proto.ProtoEvent;
import org.marid.service.proto.ProtoObject;
import org.marid.service.util.MapUtil;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public class PpContext extends ProtoObject {

    protected final PpService service;
    protected final TreeMap<String, PpBus> busMap = new TreeMap<>();
    protected final ScheduledThreadPoolExecutor timer;
    protected final Descriptor descriptor;

    public PpContext(@Nonnull PpService ppService, @Nonnull String name, @Nonnull Map<String, Object> map) {
        super(null, name, map);
        service = ppService;
        MapUtil.children(map, "buses").forEach((k, v) -> busMap.put(MapUtil.name(k), new PpBus(this, k, v)));
        descriptor = f(map, "descriptor", Descriptor.class, Descriptor.DEFAULT);
        timer = new ScheduledThreadPoolExecutor(descriptor.threads(this));
    }

    public PpService getService() {
        return service;
    }

    @Override
    public PpContext getContext() {
        return this;
    }

    @Override
    protected void init() {
        super.init();
        busMap.values().forEach(PpBus::init);
    }

    @Override
    public void start() {
        busMap.values().forEach(PpBus::start);
        fireEvent(new ProtoEvent(this, "start", null));
    }

    @Override
    public void stop() {
        busMap.values().forEach(PpBus::stop);
        fireEvent(new ProtoEvent(this, "stop", null));
    }

    @Override
    public boolean isRunning() {
        return busMap.values().stream().anyMatch(PpBus::isRunning);
    }

    @Override
    public boolean isStarted() {
        return busMap.values().stream().allMatch(PpBus::isStarted);
    }

    @Override
    public boolean isStopped() {
        return busMap.values().stream().noneMatch(PpBus::isRunning);
    }

    @Override
    public void close() throws Exception {
        busMap.values().forEach(PpBus::close);
        timer.shutdown();
        try {
            timer.awaitTermination(descriptor.shutdownTimeout(this), TimeUnit.SECONDS);
        } catch (Exception x) {
            fireEvent(new ProtoEvent(this, "close", x));
        }
        fireEvent(new ProtoEvent(this, "close", null));
    }

    protected interface Descriptor {

        Descriptor DEFAULT = new Descriptor() {};

        default int threads(PpContext context) {
            return 1;
        }

        default long shutdownTimeout(PpContext context) {
            return 60L;
        }
    }
}
