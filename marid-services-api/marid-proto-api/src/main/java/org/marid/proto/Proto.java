/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.proto;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Proto {

    String getName();

    String getId();

    Proto getParent();

    Map<String, ? extends Proto> getChildren();

    static LinkedList<String> path(Proto proto) {
        final LinkedList<String> path = new LinkedList<>();
        for (Proto p = proto; p != null; p = p.getParent()) {
            path.addFirst(p.getId());
        }
        return path;
    }

    static String label(Proto proto) {
        return proto.getName() + ": " + path(proto);
    }

    static IOException close(Map<String, ? extends Closeable> closeableMap) {
        final IOException exception = new IOException();
        for (final Map.Entry<String, ? extends Closeable> e : closeableMap.entrySet()) {
            final String id = e.getKey();
            final Closeable closeable = e.getValue();
            try {
                closeable.close();
            } catch (IOException x) {
                exception.addSuppressed(new UncheckedIOException(id, x));
            } catch (Exception x) {
                exception.addSuppressed(x);
            }
        }
        return exception;
    }
}
