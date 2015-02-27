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

import org.marid.io.DummyTransceiver;
import org.marid.io.Transceiver;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProtoBus extends AbstractProtoObject implements ProtoTaskSupport, ProtoTimerSupport {

    protected final ProtoContext parent;
    protected final ScheduledThreadPoolExecutor timer;
    protected final TreeMap<String, ProtoNode> nodeMap = new TreeMap<>();

    protected Transceiver transceiver;

    public ProtoBus(@Nonnull ProtoContext context, @Nonnull Object name, @Nonnull Map<String, Object> map) {
        super(name(name), context.getVariables(), map);
        parent = context;
        ProtoTaskSupport.putProperties(map, this);
        ProtoTimerSupport.putProperties(map, this);
        timer = new ScheduledThreadPoolExecutor(getThreads());
        putProperty(map, "transceiverParameters", Map.class);
        putProperty(map, "transceiverCreator", Function.class);
    }

    public synchronized Transceiver getTransceiver() {
        return transceiver;
    }

    public Function<Map<String, Object>, Transceiver> getTransceiverCreator() {
        return getProperty("transceiverCreator", () -> map -> DummyTransceiver.INSTANCE);
    }

    public Map<String, Object> getTransceiverParameters() {
        return getProperty("transceiverParameters", Collections::emptyMap);
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
    public synchronized void start() {
        if (transceiver == null) {
            transceiver = getTransceiverCreator().apply(getTransceiverParameters());
        }
        nodeMap.values().stream().forEach(AbstractProtoObject::start);
    }

    @Override
    public synchronized void stop() {
        nodeMap.values().stream().forEach(AbstractProtoObject::stop);
        while (isRunning()) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException x) {
                throw new IllegalStateException(x);
            }
        }
        if (transceiver != null) {
            try {
                transceiver.close();
            } catch (Exception x) {
                throw new IllegalStateException(x);
            }
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return nodeMap.values().stream().anyMatch(AbstractProtoObject::isRunning);
    }

    @Override
    public void close() throws Exception {
        stop();
        timer.shutdown();
        timer.awaitTermination(getShutdownTimeout(), TimeUnit.SECONDS);
    }
}
