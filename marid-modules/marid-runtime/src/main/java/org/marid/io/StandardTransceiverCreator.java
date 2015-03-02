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

import org.marid.io.serial.SerialTransceiver;
import org.marid.io.socket.SocketTransceiver;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public enum StandardTransceiverCreator implements Function<Map<String, Object>, Transceiver> {

    DUMMY(DummyTransceiver.CREATOR),
    SOCKET(SocketTransceiver::new),
    SERIAL(SerialTransceiver::new);

    private final Function<Map<String, Object>, Transceiver> function;

    private StandardTransceiverCreator(Function<Map<String, Object>, Transceiver> function) {
        this.function = function;
    }

    @Override
    public Transceiver apply(Map<String, Object> map) {
        return function.apply(map);
    }
}
