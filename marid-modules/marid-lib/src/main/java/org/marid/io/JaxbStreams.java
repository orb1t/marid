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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class JaxbStreams {

    private static final Logger LOG = Logger.getLogger(MethodHandles.lookup().toString());
    private static final ClassValue<JAXBContext> CLASS_VALUE = new ClassValue<JAXBContext>() {
        @Override
        protected JAXBContext computeValue(Class<?> type) {
            try {
                return JAXBContext.newInstance(type);
            } catch (Exception x) {
                warning(LOG, "Unable to create JAXB context for {0}", type);
                return null;
            }
        }
    };

    public static <T> T read(JAXBContext ctx, Class<T> type, DataInput di) throws Exception {
        final byte[] data = new byte[di.readInt()];
        di.readFully(data);
        try (final GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(data))) {
            final Unmarshaller u = ctx.createUnmarshaller();
            return type.cast(u.unmarshal(gzis));
        }
    }

    public static <O extends OutputStream & DataOutput> void write(JAXBContext ctx, O os, Object o) throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        try (final GzipOutputStream gzos = new GzipOutputStream(bos, 8192, false)) {
            m.marshal(o, gzos);
        }
        os.writeInt(bos.size());
        bos.writeTo(os);
    }

    public static <T> T read(Class<T> type, DataInput dis) throws Exception {
        return read(CLASS_VALUE.get(type), type, dis);
    }

    public static <O extends OutputStream & DataOutput> void write(O os, Object o) throws Exception {
        write(CLASS_VALUE.get(o.getClass()), os, o);
    }

    public static void writeXml(JAXBContext ctx, OutputStream os, Object o) throws Exception {
        final Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(o, os);
    }

    public static void writeXml(JAXBContext ctx, Path path, Object o) throws Exception {
        try (final OutputStream os = Files.newOutputStream(path)) {
            writeXml(ctx, os, o);
        }
    }

    public static void writeXml(OutputStream os, Object o) throws Exception {
        writeXml(CLASS_VALUE.get(o.getClass()), os, o);
    }

    public static void writeXml(Path path, Object o) throws Exception {
        writeXml(CLASS_VALUE.get(o.getClass()), path, o);
    }

    public static <T> T readXml(JAXBContext ctx, Class<T> type, InputStream inputStream) throws Exception {
        final Unmarshaller u = ctx.createUnmarshaller();
        return type.cast(u.unmarshal(inputStream));
    }

    public static <T> T readXml(Class<T> type, Path path) throws Exception {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            return readXml(CLASS_VALUE.get(type), type, inputStream);
        }
    }
}
