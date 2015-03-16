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

package org.marid.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class BinStreams {

    public static String read(URL url) throws IOException {
        try (final InputStreamReader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
            final char[] buf = new char[1024];
            final StringBuilder builder = new StringBuilder();
            while (true) {
                final int n = reader.read(buf);
                if (n < 0) {
                    break;
                }
                builder.append(buf, 0, n);
            }
            return builder.toString();
        }
    }

    public static Runnable consumeLinesTask(InputStream inputStream, Charset charset, Consumer<String> consumer) {
        return () -> {
            try {
                final BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, charset));
                while (true) {
                    final String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    consumer.accept(line);
                }
            } catch (Exception x) {
                throw new IllegalStateException(x);
            } finally {
                consumer.accept(null);
            }
        };
    }

    public static Runnable consumeLinesTask(InputStream inputStream, Consumer<String> consumer) {
        return consumeLinesTask(inputStream, Charset.defaultCharset(), consumer);
    }

    public static <T> T invoke(Socket socket, IoCallable<T> callable) throws IOException {
        try {
            return callable.call();
        } catch (SocketException x) {
            if (socket.isClosed()) {
                final ClosedChannelException cx = new ClosedChannelException();
                cx.initCause(x);
                throw cx;
            } else {
                throw x;
            }
        }
    }
}
