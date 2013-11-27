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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Dmitry Ovchinnikov
 */
public class JaxbStreams {

    public static <T> T read(JAXBContext context, Class<T> type, DataInputStream dis) throws Exception {
        final byte[] data = new byte[dis.readInt()];
        dis.readFully(data);
        try (final GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(data))) {
            final Unmarshaller u = context.createUnmarshaller();
            return type.cast(u.unmarshal(gzis));
        }
    }

    public static void write(JAXBContext context, DataOutputStream dos, Object object) throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        try (final GzipOutputStream gzos = new GzipOutputStream(bos, 8192, false)) {
            m.marshal(object, gzos);
        }
        dos.writeInt(bos.size());
        bos.writeTo(dos);
    }
}
