/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.xml;

import org.marid.io.FastArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;
import java.io.*;

import static org.marid.xml.XmlBind.JAXB;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlTransient
public abstract class XmlObject<T extends XmlObject<T>> implements Cloneable, Serializable {

    @SuppressWarnings({"unchecked", "CloneDoesntCallSuperClone"})
    @Override
    public T clone() {
        final JAXBContext context = JAXB.get(getClass());
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final FastArrayOutputStream os = new FastArrayOutputStream();
            marshaller.marshal(this, os);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(os.getSharedInputStream());
        } catch (JAXBException jaxbException) {
            throw new IllegalStateException(jaxbException);
        }
    }

    @Override
    public int hashCode() {
        final JAXBContext context = JAXB.get(getClass());
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final FastArrayOutputStream os = new FastArrayOutputStream();
            marshaller.marshal(this, os);
            return os.hashCode();
        } catch (JAXBException jaxbException) {
            throw new IllegalStateException(jaxbException);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final JAXBContext context = JAXB.get(getClass());
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final FastArrayOutputStream os1 = new FastArrayOutputStream();
            final FastArrayOutputStream os2 = new FastArrayOutputStream();
            marshaller.marshal(this, os1);
            marshaller.marshal(obj, os2);
            return os1.equals(os2);
        } catch (JAXBException jaxbException) {
            throw new IllegalStateException(jaxbException);
        }
    }

    protected Object writeReplace() throws ObjectStreamException {
        final JAXBContext context = JAXB.get(getClass());
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshaller.marshal(this, os);
            return new XmlObjectProxy(os.toByteArray(), getClass());
        } catch (JAXBException exception) {
            throw new WriteAbortedException("JAXB error", exception);
        }
    }

    private static class XmlObjectProxy implements Serializable {

        private final byte[] data;
        private final Class<?> type;

        private XmlObjectProxy(byte[] data, Class<?> type) {
            this.data = data;
            this.type = type;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                final JAXBContext context = JAXB.get(type);
                final Unmarshaller unmarshaller = context.createUnmarshaller();
                return unmarshaller.unmarshal(new ByteArrayInputStream(data));
            } catch (Exception jaxbException) {
                final StreamCorruptedException exception = new StreamCorruptedException();
                exception.initCause(jaxbException);
                throw exception;
            }
        }
    }
}
