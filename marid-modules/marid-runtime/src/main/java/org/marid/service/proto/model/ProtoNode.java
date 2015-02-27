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

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProtoNode extends AbstractProtoObject implements ProtoTaskSupport {

    protected final AbstractProtoObject parent;
    protected ScheduledFuture<?> task;

    public ProtoNode(@Nonnull ProtoBus bus, @Nonnull Object name, @Nonnull Map<String, Object> map) {
        this((AbstractProtoObject) bus, name, map);
    }

    public ProtoNode(@Nonnull ProtoNode node, @Nonnull Object name, @Nonnull Map<String, Object> map) {
        this((AbstractProtoObject) node, name, map);
    }

    private ProtoNode(@Nonnull AbstractProtoObject object, @Nonnull Object name, @Nonnull Map<String, Object> map) {
        super(name, object.getVariables(), map);
        parent = object;
        ProtoTaskSupport.putProperties(map, this);
    }

    @Nonnull
    public ProtoBus getBus() {
        for (AbstractProtoObject object = parent; object != null; object = object.getParent()) {
            if (object instanceof ProtoBus) {
                return (ProtoBus) object;
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public AbstractProtoObject getParent() {
        return parent;
    }

    @Override
    public ProtoContext getContext() {
        return getBus().getContext();
    }

    @Override
    public synchronized void start() {
        if (task == null) {

        }
    }

    @Override
    public synchronized void stop() {
        if (task != null) {
            task.cancel(isInterruptTask());
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return task != null && !task.isDone();
    }

    @Override
    public void close() {
    }
}
