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
import org.marid.service.proto.ProtoObject;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class PbNode extends ProtoObject {

    protected final Descriptor descriptor;
    protected final ConcurrentLinkedQueue<Transceiver> transceivers = new ConcurrentLinkedQueue<>();
    protected final TransceiverServer transceiverServer;

    protected Thread thread;

    protected PbNode(ProtoObject parent, Object name, Map<String, Object> map) {
        super(parent, name, map);
        descriptor = f(map, "descriptor", Descriptor.class, Descriptor.DEFAULT);
        transceiverServer = descriptor.server(this);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public synchronized void start() {
        try {
            transceiverServer.open();
        } catch (Exception x) {
            fireEvent("start", x);
        }
        thread = getContext().getService().getThreadFactory().newThread(() -> {
            while (isRunning()) {
                try {
                    final Transceiver transceiver = transceiverServer.accept();
                    if (transceiver == null) {
                        TimeUnit.SECONDS.sleep(descriptor.idleTimeout(this));
                    } else {
                        fireEvent("accept", transceiver);
                        getBus().executor.execute(() -> {
                            transceivers.add(transceiver);
                            try (final Transceiver t = transceiver) {
                                descriptor.processor(this, t);
                            } catch (ClosedChannelException x) {
                                fireEvent("destroyed");
                            } catch (Exception x) {
                                fireEvent("process", x);
                            } finally {
                                transceivers.remove(transceiver);
                            }
                        });
                    }
                } catch (ClosedChannelException x) {
                    fireEvent("destroyed");
                    return;
                } catch (Exception x) {
                    fireEvent("run", x);
                    LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(descriptor.errorTimeout(this)));
                }
            }
        });
        thread.start();
        fireEvent("start");
    }

    @Override
    public synchronized void stop() {
        try {
            transceiverServer.close(); // allow to unfreeze the node's thread on a blocking io operation
        } catch (Exception x) {
            fireEvent("stop", x);
        }
        if (thread != null) {
            try {
                thread.interrupt();
                thread.join();
            } catch (Exception x) {
                fireEvent("stop", x);
            } finally {
                thread = null;
            }
        }
        fireEvent("stop");
    }

    @Override
    public synchronized boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    @Override
    public synchronized boolean isStarted() {
        return thread != null;
    }

    @Override
    public synchronized boolean isStopped() {
        return thread == null;
    }

    @Override
    public PbNode getChild(String name) {
        return null;
    }

    @Override
    public PbContext getContext() {
        return getBus().getContext();
    }

    public PbBus getBus() {
        for (ProtoObject object = getParent(); object != null; object = object.getParent()) {
            if (object instanceof PbBus) {
                return (PbBus) object;
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public synchronized void close() {
        for (final Transceiver transceiver : transceivers) {
            try {
                transceiver.close();
            } catch (Exception x) {
                fireEvent("close", x, transceiver);
            }
        }
        stop();
        fireEvent("close");
    }

    protected interface Descriptor {

        Descriptor DEFAULT = new Descriptor() {};

        default TransceiverServer server(PbNode bus) {
            return DummyTransceiverServer.INSTANCE;
        }

        default void processor(PbNode bus, Transceiver transceiver) throws IOException {
        }

        default long errorTimeout(PbNode bus) {
            return 60L;
        }

        default long idleTimeout(PbNode bus) {
            return 1L;
        }
    }
}
