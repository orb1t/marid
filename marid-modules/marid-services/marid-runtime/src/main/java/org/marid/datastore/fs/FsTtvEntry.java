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

package org.marid.datastore.fs;

import groovy.json.JsonSlurper;
import org.marid.io.FastArrayOutputStream;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.channels.Channels.newReader;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Calendar.*;

/**
 * @author Dmitry Ovchinnikov
 */
class FsTtvEntry implements Closeable {

    private static final Set<? extends OpenOption> FOPTS = EnumSet.of(CREATE, WRITE, READ);

    final int bufferSize;
    final Path file;
    final FileChannel ch;
    final TreeMap<Date, FsTtvRecord> records = new TreeMap<>();
    final Calendar calendar;
    final ByteBuffer buffer;
    final CharsetDecoder decoder = UTF_8.newDecoder();
    final CharsetEncoder encoder = UTF_8.newEncoder();
    final JsonSlurper slurper = new JsonSlurper();

    FsTtvEntry(Path path, int bufSize) throws IOException {
        bufferSize = bufSize;
        buffer = ByteBuffer.allocateDirect(bufferSize);
        file = path;
        ch = FileChannel.open(file, FOPTS);
        final String f = file.getFileName().toString();
        calendar = new GregorianCalendar(
                parseInt(f.substring(0, 4)),
                parseInt(f.substring(5, 7)) - 1,
                parseInt(f.substring(8, 10)));
        final BufferedReader r = new BufferedReader(newReader(ch, decoder, bufferSize));
        long pos = 0L;
        while (true) {
            final String line = r.readLine();
            if (line == null) {
                break;
            }
            final int maxCount = (int) (line.length() * encoder.maxBytesPerChar() * 2);
            final int len;
            if (maxCount <= bufferSize) {
                buffer.clear();
                final CoderResult cr = encoder.encode(CharBuffer.wrap(line), buffer, true);
                if (cr.isError()) {
                    cr.throwException();
                }
                len = buffer.position();
            } else {
                final ByteBuffer buf = encoder.encode(CharBuffer.wrap(line));
                len = buf.limit();
            }
            calendar.set(HOUR_OF_DAY, parseInt(line.substring(0, 2)));
            calendar.set(MINUTE, parseInt(line.substring(3, 5)));
            calendar.set(SECOND, parseInt(line.substring(6, 8)));
            records.put(calendar.getTime(), new FsTtvRecord(pos, len));
            pos += len + 1;
        }
        assert ch.size() == ch.position();
    }

    void remove(Date from, boolean fromInc, Date to, boolean toInc) throws IOException {
        for (final Date date : records.subMap(from, fromInc, to, toInc).keySet()) {
            remove(date);
        }
    }

    boolean remove(Date date) throws IOException {
        final FsTtvRecord r = records.remove(date);
        if (r == null) {
            return false;
        }
        ch.position(r.position);
        long pos = r.position + r.length + 1;
        final long size = ch.size();
        while (pos < size) {
            pos += ch.transferTo(pos, size - pos, ch);
        }
        ch.truncate(size - r.length - 1);
        ch.position(ch.size());
        return true;
    }

    String text(FsTtvRecord record) throws IOException {
        final int len = record.length - 9;
        if (len <= bufferSize) {
            buffer.limit(len);
            buffer.position(0);
            ch.position(record.position + 9);
            while (buffer.position() < len) {
                final int n = ch.read(buffer);
                if (n < 0) {
                    throw new EOFException();
                }
            }
            buffer.flip();
            ch.position(ch.size());
            return decoder.decode(buffer).toString();
        } else {
            final FastArrayOutputStream faos = new FastArrayOutputStream(len);
            final WritableByteChannel wbch = Channels.newChannel(faos);
            final long pos = record.position + 9;
            while (faos.size() < len) {
                ch.transferTo(pos + faos.size(), len - faos.size(), wbch);
            }
            return decoder.decode(faos.getSharedByteBuffer()).toString();
        }
    }

    void put(Date date, String value, boolean insert, boolean update) throws IOException {
        if (insert) {
            if (update) {
                remove(date);
            }
        } else {
            if (!remove(date)) {
                throw new IllegalStateException(toString(date) + " does not exist");
            }
        }
        calendar.setTime(date);
        final StringBuilder builder = new StringBuilder(10 + value.length());
        {
            final int hour = calendar.get(HOUR_OF_DAY);
            if (hour < 10) {
                builder.append('0');
            }
            builder.append(hour);
        }
        builder.append('-');
        {
            final int minute = calendar.get(MINUTE);
            if (minute < 10) {
                builder.append('0');
            }
            builder.append(minute);
        }
        builder.append('-');
        {
            final int second = calendar.get(SECOND);
            if (second < 10) {
                builder.append('0');
            }
            builder.append(second);
        }
        builder.append('\t').append(value).append('\n');
        final long position = ch.position();
        final CharBuffer charBuffer = CharBuffer.wrap(builder);
        int length = 0;
        while (true) {
            buffer.clear();
            final CoderResult cr = encoder.encode(charBuffer, buffer, true);
            if (cr.isOverflow()) {
                length += buffer.position();
                buffer.flip();
                ch.write(buffer);
                continue;
            }
            if (cr.isUnderflow()) {
                length += buffer.position();
                buffer.flip();
                ch.write(buffer);
                break;
            }
            if (cr.isError()) {
                cr.throwException();
            }
        }
        final FsTtvRecord old = records.put(date, new FsTtvRecord(position, length - 1));
        if (old != null) {
            throw new IllegalStateException(toString(date) + " already exists");
        }
    }

    @Override
    public void close() throws IOException {
        ch.close();
    }

    @Override
    public String toString() {
        return "Entry " + file;
    }

    String toString(Date date) {
        return toString() + " " + new Timestamp(date.getTime());
    }
}
