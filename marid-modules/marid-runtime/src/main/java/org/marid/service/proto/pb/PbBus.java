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

import org.marid.io.DummyTransceiverServer;
import org.marid.io.Transceiver;
import org.marid.io.TransceiverServer;
import org.marid.service.proto.ProtoEvent;
import org.marid.service.proto.ProtoObject;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class PbBus extends ProtoObject {

    protected final Descriptor descriptor;
    protected final ThreadPoolExecutor executor;

    protected TransceiverServer transceiverServer;
    protected Thread thread;

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
    }

    @Override
    public PbContext getParent() {
        return (PbContext) parent;
    }

    @Override
    public synchronized void start() {
        if (transceiverServer == null) {
            try {
                transceiverServer = descriptor.transceiverServer(this, descriptor.transceiverServerParams(this));
            } catch (Exception x) {
                setChanged();
                notifyObservers(new ProtoEvent(this, "start", x));
            }
        }
        thread = getContext().getService().getThreadFactory().newThread(() -> {
            Transceiver old = null;
            while (isRunning()) {
                try {
                    final Transceiver transceiver = transceiverServer.accept();
                    if (transceiver == old) {
                        TimeUnit.SECONDS.sleep(descriptor.idleTimeout(this));
                        continue;
                    }
                    old = transceiver;
                    executor.execute(() -> {
                        try (final Transceiver t = transceiver) {
                            descriptor.processor(this, t);
                        } catch (Exception x) {
                            setChanged();
                            notifyObservers(new ProtoEvent(this, "process", x));
                        }
                    });
                } catch (Exception x) {
                    setChanged();
                    notifyObservers(new ProtoEvent(this, "run", x));
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(descriptor.errorTimeout(this)));
                }
            }
        });
        thread.start();
        setChanged();
        notifyObservers(new ProtoEvent(this, "start", null));
    }

    @Override
    public synchronized void stop() {
        if (transceiverServer != null) {
            try {
                transceiverServer.close();
            } catch (Exception x) {
                setChanged();
                notifyObservers(new ProtoEvent(this, "stop", x));
            } finally {
                transceiverServer = null;
            }
            try {
                thread.join();
            } catch (Exception x) {
                setChanged();
                notifyObservers(new ProtoEvent(this, "stop", x));
            } finally {
                thread = null;
            }
        }
        setChanged();
        notifyObservers(new ProtoEvent(this, "stop", null));
    }

    @Override
    public synchronized boolean isRunning() {
        return transceiverServer != null;
    }

    @Override
    public synchronized boolean isStarted() {
        return thread.isAlive();
    }

    @Override
    public synchronized boolean isStopped() {
        return thread == null;
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

        default Map<String, Object> transceiverServerParams(PbBus bus) {
            return Collections.emptyMap();
        }

        default TransceiverServer transceiverServer(PbBus bus, Map<String, Object> params) throws InterruptedException {
            return DummyTransceiverServer.INSTANCE;
        }

        default void processor(PbBus bus, Transceiver transceiver) {
        }

        default long errorTimeout(PbBus bus) {
            return 60L;
        }

        default long idleTimeout(PbBus bus) {
            return 1L;
        }
    }
}
