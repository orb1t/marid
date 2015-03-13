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
import org.marid.service.proto.pb.PbBus

def array = [10, 20, 30, 40];

[
    data: [
        logging: [
            delegateLogging: true
        ],
        buses: [
            bus1: [
                onInit: {

                },
                descriptor: [
                    threads: 1
                ],
                nodes: [
                    node1: [
                        descriptor: [
                            server: {new SocketTransceiverServer([:])},
                            processor: {PbBus b, Transceiver t ->
                                while (b.running) {
                                    def data = t.data.read({buf -> buf.remaining() != 4 ? null : array[buf.getInt(0)]});
                                    if (data != null) {
                                        t.data.writeInt(data as int);
                                    }
                                }
                            }
                        ]
                    ]
                ]
            ]
        ]
    ]
]