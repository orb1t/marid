/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.marid.logging.Logging;

/**
 * Launch class.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class Marid implements Runnable {

    /**
     * Launch entry point.
     *
     * @param args Command-line arguments.
     * @throws Exception An exception.
     */
    public static void main(String... args) throws Exception {
        String cmd = "start";
        if (args.length > 0) {
            cmd = args[args.length - 1];
        }
        switch (cmd) {
            case "start":
                Logging.init(Marid.class, "/log.properties");
                startShutdownThread().join();
                break;
            case "stop":
                sendShutdownCommand();
                break;
        }

    }

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception x) {
            LogRecord lr = new LogRecord(
                    Level.WARNING, "Unable to listen shutdown port {0}");
            lr.setThrown(x);
            lr.setParameters(new Object[]{Context.SHUTDOWN_PORT});
            Log.l.log(lr);
        }
    }

    private void doRun() throws Exception {
        try (DatagramSocket ds = new DatagramSocket(null)) {
            ds.setReuseAddress(true);
            ds.bind(new InetSocketAddress(Context.SHUTDOWN_PORT));
            while (true) {
                byte[] buf = new byte[1024];
                DatagramPacket p = new DatagramPacket(buf, 1024);
                ds.receive(p);
                if (Context.LOSFF && !p.getAddress().isLoopbackAddress()) {
                    continue;
                }
                String data = new String(buf, 0, p.getLength(),
                        StandardCharsets.ISO_8859_1);
                if (Log.l.isLoggable(Level.INFO)) {
                    Log.l.log(Level.INFO, "Datagram packet {0} from {1}",
                            new Object[]{data, p.getAddress().toString()});
                }
                if (data.startsWith(Context.APP_ID)) {
                    data = data.substring(Context.APP_ID.length()).trim();
                    if ("shutdown".equals(data)) {
                        break;
                    }
                }
            }
        }
    }

    private static Thread startShutdownThread() {
        Marid l = new Marid();
        Thread t = new Thread(l, "shutdown-thread");
        t.start();
        return t;
    }

    private static void sendShutdownCommand() {
        try {
            DatagramSocket ds = new DatagramSocket();
            String data = Context.APP_ID + " shutdown";
            byte[] buf = data.getBytes(StandardCharsets.ISO_8859_1);
            DatagramPacket p = new DatagramPacket(buf, buf.length);
            p.setSocketAddress(new InetSocketAddress(
                    InetAddress.getLoopbackAddress(), Context.SHUTDOWN_PORT));
            ds.send(p);
            if (ds.isConnected()) {
                ds.disconnect();
            }
        } catch (Exception x) {
            Log.l.log(Level.WARNING, "Unable to send shutdown sequence", x);
        }
    }

    private static class Log {

        static final Logger l = Logger.getLogger("sys", "res.messages");
    }
}
