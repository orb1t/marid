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

import org.marid.service.proto.ProtoObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.ToLongFunction;

/**
 * @author Dmitry Ovchinnikov
 */
public class PpContext extends ProtoObject<PpContext> {

    protected final PpService service;
    protected final TreeMap<String, PpBus> busMap = new TreeMap<>();
    protected final ScheduledThreadPoolExecutor timer;
    protected final ToLongFunction<PpContext> shutdownTimeout;

    public PpContext(@Nonnull PpService ppService, @Nonnull String name, @Nonnull Descriptor descriptor) {
        super(null, name, descriptor);
        service = ppService;
        shutdownTimeout = descriptor::shutdownTimeout;
        descriptor.buses().forEach((k, v) -> busMap.put(k, new PpBus(this, k, v)));
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
        fireEvent("start");
    }

    @Override
    public void stop() {
        busMap.values().forEach(PpBus::stop);
        fireEvent("stop");
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
    public PpBus getAt(String name) {
        return busMap.get(name);
    }

    @Override
    public void close() throws Exception {
        busMap.values().forEach(PpBus::close);
        timer.shutdown();
        try {
            if (!timer.awaitTermination(shutdownTimeout.applyAsLong(this), TimeUnit.SECONDS)) {
                throw new TimeoutException();
            }
        } catch (Exception x) {
            fireEvent("close", x);
        }
        fireEvent("close");
    }

    public interface Descriptor extends ProtoObject.Descriptor<PpContext> {

        default int threads(PpContext context) {
            return 1;
        }

        default long shutdownTimeout(PpContext context) {
            return 60L;
        }

        default Map<String, PpBus.Descriptor> buses() {
            return Collections.emptyMap();
        }
    }
}
