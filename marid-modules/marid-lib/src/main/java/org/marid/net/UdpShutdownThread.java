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

package org.marid.net;

import org.marid.logging.LogSupport;
import org.marid.util.ShutdownCodes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Dmitry Ovchinnikov
 */
public class UdpShutdownThread extends Thread implements LogSupport {

    private static final int PACKET_SIZE = 128;

    private final InetSocketAddress bindAddress;
    private volatile int exitCode = ShutdownCodes.UDP_ERROR_SHUTDOWN;

    public UdpShutdownThread(ThreadGroup threadGroup, Runnable task, String name, InetSocketAddress bindAddress) {
        super(threadGroup, task, name);
        this.bindAddress = bindAddress;
    }

    public UdpShutdownThread(String name, Runnable task, InetSocketAddress bindAddress) {
        super(task, name);
        this.bindAddress = bindAddress;
    }

    @Override
    public void run() {
        try {
            try (final DatagramSocket socket = new DatagramSocket(bindAddress)) {
                log(INFO, "Starting {0} on {1}", getName(), bindAddress);
                final DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
                super.run();
                while (true) {
                    socket.receive(packet);
                    final InetSocketAddress peer = (InetSocketAddress) packet.getSocketAddress();
                    if (peer.getAddress().equals(bindAddress.getAddress()) && peer.getPort() != bindAddress.getPort()) {
                        final String name = new String(packet.getData(), packet.getOffset(), packet.getLength(), UTF_8);
                        if (name.trim().equals(getName())) {
                            log(INFO, "Shutdown {0} by {1}", getName(), peer);
                            exitCode = 0;
                            break;
                        }
                    } else {
                        log(INFO, "Bad peer {0}", peer);
                    }
                }
            }
        } catch (Exception x) {
            log(WARNING, "UDP error: {0}", x, getName());
        }
    }

    public int getExitCode() {
        return exitCode;
    }

    public static void sendShutdownSequence(InetSocketAddress address, String name) throws IOException {
        try (final DatagramSocket socket = new DatagramSocket()) {
            try {
                socket.connect(address);
                final byte[] src = name.getBytes(UTF_8);
                final byte[] data = new byte[PACKET_SIZE];
                Arrays.fill(data, (byte) ' ');
                System.arraycopy(src, 0, data, 0, src.length);
                socket.send(new DatagramPacket(data, data.length));
            } finally {
                socket.disconnect();
            }
        }
    }
}
