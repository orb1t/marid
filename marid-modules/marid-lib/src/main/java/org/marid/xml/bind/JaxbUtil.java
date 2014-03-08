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

package org.marid.xml.bind;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class JaxbUtil {

    private static final ClassValue<JAXBContext> CONTEXT_CLASS_VALUE = new ClassValue<JAXBContext>() {
        @Override
        protected JAXBContext computeValue(Class<?> type) {
            try {
                return JAXBContext.newInstance(type);
            } catch (JAXBException x) {
                throw new IllegalStateException(x);
            }
        }
    };

    public static <T> T load(Class<T> type, URL url) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(type);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(url));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T load(Class<T> type, File file) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(type);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(file));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T load(Class<T> type, Path path) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(type);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(path.toFile()));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T load(Class<T> type, Reader reader) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(type);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(reader));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T load(Class<T> type, InputStream inputStream) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(type);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(inputStream));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static void save(Object object, File file, boolean fragment, boolean formatted) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(object.getClass());
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);
            marshaller.marshal(object, file);
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static void save(Object object, Path path, boolean fragment, boolean formatted) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(object.getClass());
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);
            marshaller.marshal(object, path.toFile());
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static void save(Object object, Writer writer, boolean fragment, boolean formatted) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(object.getClass());
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);
            marshaller.marshal(object, writer);
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static void save(Object object, OutputStream os, boolean fragment, boolean formatted) throws IOException {
        final JAXBContext context = CONTEXT_CLASS_VALUE.get(object.getClass());
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);
            marshaller.marshal(object, os);
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }
}
