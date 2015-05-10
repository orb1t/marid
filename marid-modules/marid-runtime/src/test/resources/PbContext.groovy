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

import org.marid.io.Transceiver
import org.marid.io.socket.SocketTransceiverServer
import org.marid.service.proto.pb.PbNode

def array = [10, 20, 30, 40];

[
    data: [
        buses: [
            bus1: [
                threads: 2,
                nodes  : [
                    node1: [
                        server   : new SocketTransceiverServer([:]),
                        processor: { PbNode b, Transceiver t ->
                            while (b.running) {
                                def data = t.data.rule(4, { buf -> array[buf.getInt()] }).read();
                                if (data != null) {
                                    t.data.write([data as int]);
                                }
                            }
                        }
                    ],
                    node2: [
                        server   : new SocketTransceiverServer([:]),
                        processor: { PbNode b, Transceiver t ->
                            while (b.running) {
                                def data = t.data.rule(4, { buf -> array[buf.getInt()] }).read();
                                if (data != null) {
                                    t.data << [data as int];
                                }
                            }
                        }
                    ]
                ]
            ]
        ]
    ]
]