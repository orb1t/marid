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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class PbNode extends ProtoObject {

    protected final Descriptor descriptor;

    protected TransceiverServer transceiverServer;
    protected Thread thread;

    protected PbNode(ProtoObject parent, Object name, Map<String, Object> map) {
        super(parent, name, map);
        descriptor = f(map, "descriptor", Descriptor.class, Descriptor.DEFAULT);
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
        if (transceiverServer != null) {
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
                        getBus().executor.execute(() -> {
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
        }
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
        }
        if (thread != null) {
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
    public synchronized void close() throws Exception {
        stop();
        setChanged();
        notifyObservers(new ProtoEvent(this, "close", null));
    }

    protected interface Descriptor {

        Descriptor DEFAULT = new Descriptor() {
        };

        default Map<String, Object> transceiverServerParams(PbNode bus) {
            return Collections.emptyMap();
        }

        default TransceiverServer transceiverServer(PbNode bus, Map<String, Object> params) throws InterruptedException {
            return DummyTransceiverServer.INSTANCE;
        }

        default void processor(PbNode bus, Transceiver transceiver) {
        }

        default long errorTimeout(PbNode bus) {
            return 60L;
        }

        default long idleTimeout(PbNode bus) {
            return 1L;
        }
    }
}
