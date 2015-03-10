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

import org.marid.service.proto.ProtoEvent;
import org.marid.service.proto.ProtoObject;
import org.marid.service.util.MapUtil;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class PbBus extends ProtoObject {

    protected final Descriptor descriptor;
    protected final ThreadPoolExecutor executor;
    protected final TreeMap<String, PbNode> nodeMap = new TreeMap<>();

    protected PbBus(PbContext context, Object name, Map<String, Object> map) {
        super(context, name, map);
        descriptor = f(map, "descriptor", Descriptor.class, Descriptor.DEFAULT);
        executor = new ThreadPoolExecutor(
                descriptor.threads(this),
                descriptor.maxThreads(this),
                descriptor.keepAliveTime(this),
                TimeUnit.SECONDS,
                descriptor.queue(this),
                context.getService().getThreadFactory(),
                descriptor.rejectedExecutionHandler(this));
        MapUtil.children(map, "nodes").forEach((k, v) -> nodeMap.put(MapUtil.name(k), new PbNode(this, k, v)));
    }

    @Override
    public PbContext getParent() {
        return (PbContext) parent;
    }

    @Override
    public synchronized void start() {
        nodeMap.values().forEach(PbNode::start);
        setChanged();
        notifyObservers(new ProtoEvent(this, "start", null));
    }

    @Override
    public synchronized void stop() {
        nodeMap.values().forEach(PbNode::stop);
        setChanged();
        notifyObservers(new ProtoEvent(this, "stop", null));
    }

    @Override
    public synchronized boolean isRunning() {
        return nodeMap.values().stream().anyMatch(PbNode::isRunning);
    }

    @Override
    public synchronized boolean isStarted() {
        return nodeMap.values().stream().allMatch(PbNode::isStarted);
    }

    @Override
    public synchronized boolean isStopped() {
        return nodeMap.values().stream().noneMatch(PbNode::isRunning);
    }

    @Override
    public PbContext getContext() {
        return getParent();
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        try {
            executor.awaitTermination(descriptor.shutdownTimeout(this), TimeUnit.SECONDS);
        } catch (Exception x) {
            setChanged();
            notifyObservers(new ProtoEvent(this, "close", x));
        }
    }

    protected interface Descriptor {

        Descriptor DEFAULT = new Descriptor() {
        };

        default RejectedExecutionHandler rejectedExecutionHandler(PbBus bus) {
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }

        default int threads(PbBus bus) {
            return Runtime.getRuntime().availableProcessors();
        }

        default int maxThreads(PbBus bus) {
            return Runtime.getRuntime().availableProcessors();
        }

        default BlockingQueue<Runnable> queue(PbBus bus) {
            return new LinkedBlockingQueue<>();
        }

        default long keepAliveTime(PbBus bus) {
            return 0L;
        }

        default long shutdownTimeout(PbBus bus) {
            return 60L;
        }
    }
}
