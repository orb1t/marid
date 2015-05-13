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
import java.util.concurrent.*;
import java.util.function.ToLongFunction;

/**
 * @author Dmitry Ovchinnikov
 */
public class PbBus extends ProtoObject<PbBus> {

    protected final ThreadPoolExecutor executor;
    protected final TreeMap<String, PbNode> nodeMap = new TreeMap<>();
    protected final ToLongFunction<PbBus> shutdownTimeout;

    protected PbBus(PbContext context, String name, Descriptor descriptor) {
        super(context, name, descriptor);
        executor = new ThreadPoolExecutor(
                descriptor.threads(this),
                descriptor.maxThreads(this),
                descriptor.keepAliveTime(this),
                TimeUnit.SECONDS,
                descriptor.queue(this),
                context.getService().getThreadFactory(),
                descriptor.rejectedExecutionHandler(this));
        shutdownTimeout = descriptor::shutdownTimeout;
        descriptor.nodes().forEach((k, v) -> nodeMap.put(k, new PbNode(this, k, v)));
    }

    @Override
    protected void init() {
        super.init();
        nodeMap.values().forEach(PbNode::init);
    }

    @Override
    public PbContext getParent() {
        return (PbContext) parent;
    }

    @Override
    public void start() {
        synchronized (this) {
            nodeMap.values().forEach(PbNode::start);
        }
        fireEvent("start");
    }

    @Override
    public void stop() {
        synchronized (this) {
            nodeMap.values().forEach(PbNode::stop);
        }
        fireEvent("stop");
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
    public PbNode getAt(String name) {
        return nodeMap.get(name);
    }

    @Override
    public PbContext getContext() {
        return getParent();
    }

    @Override
    public void close() {
        nodeMap.values().forEach(PbNode::close);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(shutdownTimeout.applyAsLong(this), TimeUnit.SECONDS)) {
                throw new TimeoutException();
            }
        } catch (Exception x) {
            fireEvent("close", x);
        }
        fireEvent("close");
    }

    public interface Descriptor extends ProtoObject.Descriptor<PbBus> {

        default RejectedExecutionHandler rejectedExecutionHandler(PbBus bus) {
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }

        default int threads(PbBus bus) {
            return Runtime.getRuntime().availableProcessors();
        }

        default int maxThreads(PbBus bus) {
            return threads(bus);
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

        default Map<String, PbNode.Descriptor> nodes() {
            return Collections.emptyMap();
        }
    }
}
