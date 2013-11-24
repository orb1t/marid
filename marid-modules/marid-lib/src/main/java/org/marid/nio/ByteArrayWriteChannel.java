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

package org.marid.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @author Dmitry Ovchinnikov
 */
public class ByteArrayWriteChannel implements WritableByteChannel {

    private final byte[] buffer;
    private final int offset;
    private final int length;
    private int written;

    public ByteArrayWriteChannel(byte[] buffer, int offset, int length) {
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    public ByteArrayWriteChannel(byte[] buf) {
        this(buf, 0, buf.length);
    }

    public ByteArrayWriteChannel(int len) {
        this(new byte[len]);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    public synchronized int size() {
        return written;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(buffer, offset, length);
    }

    public String toString(Charset charset) {
        return charset.decode(getByteBuffer()).toString();
    }

    public String toString(CharsetDecoder decoder) throws CharacterCodingException {
        return decoder.decode(getByteBuffer()).toString();
    }

    public void transferFrom(FileChannel channel, long pos) throws IOException {
        while (written < length) {
            channel.transferTo(pos + written, length - written, this);
        }
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public synchronized int write(ByteBuffer src) throws IOException {
        final int count = Math.min(length - written, src.remaining());
        src.get(buffer, offset + written, count);
        written += count;
        return count;
    }
}
