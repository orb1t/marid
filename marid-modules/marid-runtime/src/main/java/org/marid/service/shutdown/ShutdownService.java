/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.service.shutdown;

import org.marid.Marid;
import org.marid.service.AbstractMaridService;
import org.marid.service.MaridServiceConfig;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ShutdownService extends AbstractMaridService {

    protected final DatagramSocket socket;
    protected final String selector;

    public ShutdownService(ShutdownServiceConfig config) throws IOException {
        super(config);
        selector = config.selector();
        socket = new DatagramSocket(config.port(), InetAddress.getLoopbackAddress());
    }

    public ShutdownService() throws IOException {
        this(MaridServiceConfig.config());
    }

    public ShutdownService(String prefix, Environment environment) throws IOException {
        this(MaridServiceConfig.config(prefix, environment));
    }

    public int getShutdownPort() {
        return socket.getLocalPort();
    }

    public String getSelector() {
        return selector;
    }

    @Override
    public void close() throws Exception {
        try {
            socket.close();
        } finally {
            super.close();
        }
    }

    @Override
    public void start() throws Exception {
        executor.execute(() -> {
            while (isRunning()) {
                try {
                    final DatagramPacket pk = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(pk);
                    if (pk.getAddress().isLoopbackAddress()) {
                        final String s = new String(pk.getData(), pk.getOffset(), pk.getLength(), UTF_8);
                        if (s.equals(selector)) {
                            new Thread(Marid.CONTEXT::close).start();
                            break;
                        }
                    }
                } catch (IOException x) {
                    warning("I/O exception", x);
                } catch (Exception x) {
                    severe("Unknown error", x);
                }
            }
        });
    }
}
