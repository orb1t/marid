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
import org.marid.nio.ByteArrayWriteChannel;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Calendar.*;
import static org.marid.util.StringUtils.patoi;

/**
 * @author Dmitry Ovchinnikov
 */
class FsTtvEntry implements Closeable {

    private static final Set<? extends OpenOption> OPTS = EnumSet.of(CREATE, WRITE, READ);

    final int bufferSize;
    final ByteBuffer buffer;
    final Path file;
    final FileChannel ch;
    final TreeMap<Date, FsTtvRecord> index = new TreeMap<>();
    final Calendar cal;
    final JsonSlurper slurper = new JsonSlurper();
    final CharsetEncoder encoder = UTF_8.newEncoder();
    final CharsetDecoder decoder = UTF_8.newDecoder();

    FsTtvEntry(Path path, int bufSize) throws IOException {
        bufferSize = bufSize;
        buffer = ByteBuffer.allocateDirect(bufferSize);
        file = path;
        ch = FileChannel.open(file, OPTS);
        final String f = file.getFileName().toString();
        cal = new GregorianCalendar(
                parseInt(f.substring(0, 4)),
                parseInt(f.substring(5, 7)) - 1,
                parseInt(f.substring(8, 10)));
        final FastArrayOutputStream faos = new FastArrayOutputStream();
        long pos = 0L;
        int r = ch.read(buffer);
        buffer.flip();
        while (r >= 0) {
            while (buffer.hasRemaining()) {
                final byte b = buffer.get();
                faos.write(b & 0xFF);
                if (b == '\n') {
                    final byte[] buf = faos.getSharedBuffer();
                    cal.set(HOUR_OF_DAY, patoi(buf, 0, 2));
                    cal.set(MINUTE, patoi(buf, 3, 2));
                    cal.set(SECOND, patoi(buf, 6, 2));
                    index.put(cal.getTime(), new FsTtvRecord(pos, faos.size()));
                    pos += faos.size();
                    faos.reset();
                }
            }
            buffer.clear();
            r = ch.read(buffer);
            buffer.flip();
        }
    }

    int remove(Date from, boolean fromInc, Date to, boolean toInc) throws IOException {
        final NavigableMap<Date, FsTtvRecord> subMap = index.subMap(from, fromInc, to, toInc);
        final int count = subMap.size();
        subMap.clear();
        return count;
    }

    boolean remove(Date date) throws IOException {
        return index.remove(date) != null;
    }

    String text(FsTtvRecord record) throws IOException {
        return extract(record.position + 9, record.length - 10);
    }

    String recordText(FsTtvRecord record) throws IOException {
        return extract(record.position, record.length);
    }

    String extract(long pos, int len) throws IOException {
        final ByteArrayWriteChannel wch = new ByteArrayWriteChannel(len);
        wch.transferFrom(ch, pos);
        return wch.toString(decoder);
    }

    void put(Date date, String value, boolean insert, boolean update) throws IOException {
        cal.setTime(date);
        final CharBuffer cb = CharBuffer.allocate(10 + value.length());
        {
            int field = cal.get(HOUR_OF_DAY);
            if (field < 10) cb.append('0');
            cb.append(Integer.toString(field)).append('-');
            field = cal.get(MINUTE);
            if (field < 10) cb.append('0');
            cb.append(Integer.toString(field)).append('-');
            field = cal.get(SECOND);
            if (field < 10) cb.append('0');
            cb.append(Integer.toString(field)).append(' ').append(value).append('\n').position(0);
        }
        final long pos = ch.position();
        int len = 0;
        for (buffer.clear(); ; ) {
            final CoderResult r = encoder.encode(cb, buffer, true);
            buffer.flip();
            len += buffer.remaining();
            do {
                ch.write(buffer);
            } while (buffer.hasRemaining());
            if (r.isOverflow()) {
                buffer.clear();
            } else if (r.isUnderflow()) {
                break;
            } else if (r.isError()) {
                r.throwException();
            } else {
                throw new IllegalStateException(r.toString());
            }
        }
        final FsTtvRecord old = index.put(date, new FsTtvRecord(pos, len));
        if (old != null) {
            if (insert && !update) {
                throw new IllegalStateException("Already exists");
            }
        } else {
            if (update) {
                throw new IllegalStateException("Not exists");
            }
        }
    }

    @Override
    public void close() throws IOException {
        final IOException e = new IOException();
        if (index.isEmpty()) {
            try {
                ch.close();
            } catch (Exception x) {
                e.addSuppressed(x);
            }
            try {
                Files.delete(file);
            } catch (Exception x) {
                e.addSuppressed(x);
            }
        } else {
            final String bakName = file.getFileName().toString() + ".bak";
            final Path bak = file.getParent().resolve(bakName);
            try (final BufferedWriter w = Files.newBufferedWriter(bak, UTF_8)) {
                for (final FsTtvRecord r : index.values()) {
                    w.append(recordText(r));
                }
            } catch (Exception x) {
                e.addSuppressed(x);
            } finally {
                try {
                    ch.close();
                } catch (Exception x) {
                    e.addSuppressed(x);
                }
                try {
                    if (Files.exists(bak)) {
                        Files.move(bak, file, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception x) {
                    e.addSuppressed(x);
                }
            }
        }
        if (e.getSuppressed().length > 0) {
            throw e;
        }
    }

    @Override
    public String toString() {
        return "Entry " + file;
    }
}
