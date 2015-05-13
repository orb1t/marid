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

package org.marid.service.proto.pb;

import org.marid.service.proto.ProtoObject;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class PbContext extends ProtoObject<PbContext> {

    protected final PbService service;
    protected final Map<String, PbBus> busMap = new TreeMap<>();

    protected PbContext(PbService service, String name, Descriptor descriptor) {
        super(null, name, descriptor);
        this.service = service;
        descriptor.buses().forEach((k, v) -> busMap.put(k, new PbBus(this, k, v)));
    }

    public PbService getService() {
        return service;
    }

    @Override
    protected void init() {
        super.init();
        busMap.values().forEach(PbBus::init);
    }

    @Override
    public synchronized void start() {
        busMap.values().forEach(PbBus::start);
        fireEvent("start");
    }

    @Override
    public synchronized void stop() {
        busMap.values().forEach(PbBus::stop);
        fireEvent("stop");
    }

    @Override
    public boolean isRunning() {
        return busMap.values().stream().anyMatch(PbBus::isRunning);
    }

    @Override
    public boolean isStarted() {
        return busMap.values().stream().allMatch(PbBus::isStarted);
    }

    @Override
    public boolean isStopped() {
        return busMap.values().stream().noneMatch(PbBus::isRunning);
    }

    @Override
    public PbBus getAt(String name) {
        return busMap.get(name);
    }

    @Override
    public PbContext getContext() {
        return this;
    }

    @Override
    public synchronized void close() throws Exception {
        busMap.values().forEach(PbBus::close);
        fireEvent("close");
    }

    public interface Descriptor extends ProtoObject.Descriptor<PbContext> {

        default Map<String, PbBus.Descriptor> buses() {
            return Collections.emptyMap();
        }
    }
}
