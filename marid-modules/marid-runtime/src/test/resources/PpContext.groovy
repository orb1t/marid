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

import groovy.transform.Field
import org.marid.io.Transceiver
import org.marid.io.socket.SocketTransceiver
import org.marid.service.proto.pb.PbService
import org.marid.service.proto.pp.PpNode
import org.springframework.beans.factory.annotation.Autowired

@Field @Autowired private PbService pbService;

[
    data: [
        buses  : [
            bus1: [
                transceiver: { new SocketTransceiver([socketAddress: new InetSocketAddress(pbService.context["bus1"]["node1"].transceiverServer.serverSocket.localPort)]) },
                nodes      : [
                    node1: [
                        period: 1,
                        task  : { PpNode node ->
                            node.bus.io({ Transceiver t ->
                                for (def i in 0..3) {
                                    t.data << [i as int];
                                    def data = t.data.rule(4, { buf -> buf.getInt() }).read();
                                    if (data != null) {
                                        node.context.vars[i.toString()] = data;
                                    }
                                }
                            })
                        }
                    ]
                ]
            ],
            bus2: [
                transceiver: { new SocketTransceiver([socketAddress: new InetSocketAddress(pbService.context["bus1"]["node2"].transceiverServer.serverSocket.localPort)]) },
                nodes      : [
                    node1: [
                        period: 1,
                        task  : { PpNode node ->
                            node.bus.io({ Transceiver t ->
                                for (def i in 4..9) {
                                    t.data << [i as int];
                                    def data = t.data.rule(4, { buf -> buf.getInt() }).read();
                                    if (data != null) {
                                        node.context.vars[i.toString()] = data;
                                    }
                                }
                            })
                        }
                    ]
                ]
            ]
        ]
    ]
]