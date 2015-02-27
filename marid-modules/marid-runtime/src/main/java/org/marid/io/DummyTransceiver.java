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

package org.marid.io;

import java.io.IOException;

/**
 * @author Dmitry Ovchinnikov
 */
public class DummyTransceiver implements Transceiver {

    public static final DummyTransceiver INSTANCE = new DummyTransceiver();

    private DummyTransceiver() {
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void open() throws IOException {
    }

    @Override
    public void write(byte[] data, int offset, int len) throws IOException {
    }

    @Override
    public int read(byte[] data, int offset, int len) throws IOException {
        return 0;
    }

    @Override
    public int available() throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {
    }
}
