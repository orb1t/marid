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

import org.marid.util.MaridClassValue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class XmlDomain {

    private static final ClassValue<JAXBContext> JAXB_DCV = new MaridClassValue<>(c -> {
        final List<Class<?>> classList = new LinkedList<>(Collections.singleton(c));
        for (final XmlDomain domain : ServiceLoader.load(XmlDomain.class)) {
            if (domain.getBaseType().isAssignableFrom(c)) {
                classList.addAll(domain.getClasses());
            }
        }
        return JAXBContext.newInstance(classList.toArray(new Class<?>[classList.size()]));
    });

    public abstract Class<?> getBaseType();

    public abstract Set<Class<?>> getClasses();

    public static JAXBContext getContext(Class<?> baseClass) {
        return JAXB_DCV.get(baseClass);
    }

    public static <T> T load(Class<T> type, URL url) {
        return load(type, type, url);
    }

    public static <T> T load(Class<T> type, Class<?> base, URL url) {
        final JAXBContext context = JAXB_DCV.get(base);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(url));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T load(Class<T> type, File file) {
        return load(type, type, file);
    }

    public static <T> T load(Class<T> type, Class<?> base, File file) {
        final JAXBContext context = JAXB_DCV.get(base);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(file));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T load(Class<T> type, Path path) throws IOException {
        return load(type, type, path);
    }

    public static <T> T load(Class<T> type, Class<?> base, Path path) {
        final JAXBContext context = JAXB_DCV.get(base);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(path.toFile()));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T load(Class<T> type, Reader reader) {
        return load(type, type, reader);
    }

    public static <T> T load(Class<T> type, Class<?> base, Reader reader) {
        final JAXBContext context = JAXB_DCV.get(base);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(reader));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T load(Class<T> type, InputStream inputStream) {
        return load(type, type, inputStream);
    }

    public static <T> T load(Class<T> type, Class<?> base, InputStream inputStream) {
        final JAXBContext context = JAXB_DCV.get(base);
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(inputStream));
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    public static void save(Object object, File file, boolean fragment, boolean formatted) {
        save(object.getClass(), object, file, fragment, formatted);
    }

    public static void save(Class<?> base, Object object, File file, boolean fragment, boolean formatted) {
        final JAXBContext context = JAXB_DCV.get(base);
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

    public static void save(Object object, Path path, boolean fragment, boolean formatted) {
        save(object.getClass(), object, path, fragment, formatted);
    }

    public static void save(Class<?> base, Object object, Path path, boolean fragment, boolean formatted) {
        final JAXBContext context = JAXB_DCV.get(base);
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

    public static void save(Object object, Writer writer, boolean fragment, boolean formatted) {
        save(object.getClass(), object, writer, fragment, formatted);
    }

    public static void save(Class<?> base, Object object, Writer writer, boolean fragment, boolean formatted) {
        final JAXBContext context = JAXB_DCV.get(base);
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

    public static void save(Object object, OutputStream os, boolean fragment, boolean formatted) {
        save(object.getClass(), object, os, fragment, formatted);
    }

    public static void save(Class<?> base, Object object, OutputStream os, boolean fragment, boolean formatted) {
        final JAXBContext context = JAXB_DCV.get(base);
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

    public static byte[] getBytes(Class<?> baseClass, Object object) {
        try {
            final JAXBContext context = XmlDomain.getContext(baseClass);
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
            marshaller.marshal(object, bos);
            return bos.toByteArray();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public static Object getObject(Class<?> baseClass, byte[] data) {
        try {
            final JAXBContext context = XmlDomain.getContext(baseClass);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return unmarshaller.unmarshal(new ByteArrayInputStream(data));
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }
}
