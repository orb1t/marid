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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketOption;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.Map;

import static org.marid.methods.PropMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SocketChannelStreams implements IOStreams {

    protected final AsynchronousSocketChannel channel;
    protected final InputStream input;
    protected final OutputStream output;

    public SocketChannelStreams(Map params) throws IOException {
        channel = AsynchronousSocketChannel.open();
        for (final SocketOption option : channel.supportedOptions()) {
            if (params.containsKey(option.name())) {
                channel.setOption(option, get(params, option.type(), option.name(), null));
            }
        }
        input = Channels.newInputStream(channel);
        output = Channels.newOutputStream(channel);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return input;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return output;
    }

    @Override
    public boolean isValid() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
