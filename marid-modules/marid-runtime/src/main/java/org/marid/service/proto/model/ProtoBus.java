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

import org.marid.io.Transceiver;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProtoBus extends AbstractProtoObject implements ProtoTaskSupport, ProtoTimerSupport {

    protected final ProtoContext parent;
    protected final ScheduledThreadPoolExecutor timer;
    protected final TreeMap<String, ProtoNode> nodeMap = new TreeMap<>();

    public ProtoBus(@Nonnull ProtoContext context, @Nonnull Object name, @Nonnull Map<String, Object> map) {
        super(name(name), context.getVariables(), map);
        parent = context;
        ProtoTaskSupport.putProperties(map, this);
        ProtoTimerSupport.putProperties(map, this);
        timer = new ScheduledThreadPoolExecutor(getThreads());
    }

    public Transceiver getTransceiver() {
        return null;
    }

    @Override
    public ProtoContext getParent() {
        return parent;
    }

    @Override
    public ProtoContext getContext() {
        return parent;
    }

    @Override
    public void start() {
        nodeMap.values().stream().forEach(AbstractProtoObject::start);
    }

    @Override
    public void stop() {
        nodeMap.values().stream().forEach(AbstractProtoObject::stop);
    }

    @Override
    public boolean isRunning() {
        return nodeMap.values().stream().anyMatch(AbstractProtoObject::isRunning);
    }

    @Override
    public void close() throws Exception {
        stop();
        timer.shutdown();
        timer.awaitTermination(getShutdownTimeout(), TimeUnit.SECONDS);
    }
}
