/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.io;

import java.net.SocketOption;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridSocketOptions {

    public static final SocketOption<Integer> SO_TIMEOUT = new MaridSocketOption<>("SO_TIMEOUT", Integer.class);
    public static final SocketOption<Integer> CONN_TIMEOUT = new MaridSocketOption<>("CONN_TIMEOUT", Integer.class);

    private static class MaridSocketOption<T> implements SocketOption<T> {

        private final String name;
        private final Class<T> type;

        private MaridSocketOption(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Class<T> type() {
            return type;
        }
    }
}
