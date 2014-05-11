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

package org.marid.bd;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.marid.util.Utils;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({
        Block.PortKey.class
})
public abstract class Block implements Serializable {

    @XmlID
    @XmlAttribute
    public final String id = Utils.getUid().toString(Character.MAX_RADIX);

    public abstract JComponent getComponent();

    public abstract Window getEditor();

    public abstract List<? extends Port> getPorts();

    public abstract String getName();

    public List<? extends Port> getPorts(PortType portType) {
        return getPorts().stream().filter(p -> p.getPortKey().portType == portType).collect(Collectors.toList());
    }

    public Port getPort(PortKey portKey) {
        return getPorts().stream().filter(p -> portKey.equals(p.getPortKey())).findFirst().get();
    }

    protected Object writeReplace() throws ObjectStreamException {
        try {
            final Marshaller marshaller = BdObjectProvider.JAXB_CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
            marshaller.marshal(this, bos);
            return new BlockProxy(bos.toByteArray());
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String toString() {
        final Set<Class<? extends Annotation>> xmlAnnotations = ImmutableSet.of(XmlElement.class, XmlAttribute.class);
        return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) {
            @Override
            protected boolean accept(Field field) {
                return Arrays.stream(field.getAnnotations()).anyMatch(a -> xmlAnnotations.contains(a.annotationType()));
            }
        }.toString();
    }

    public abstract class Port {

        public abstract PortKey getPortKey();

        public abstract Type getDataType();

        public abstract ImageIcon getIcon();

        public Block getBlock() {
            return Block.this;
        }
    }

    @XmlRootElement(name = "port")
    public static final class PortKey {

        @XmlAttribute
        public final PortType portType;

        @XmlAttribute
        public final String name;

        private PortKey() {
            this(null, null);
        }

        public PortKey(PortType portType, String name) {
            this.portType = portType;
            this.name = name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(portType, name);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PortKey) {
                final PortKey that = (PortKey) obj;
                return Arrays.equals(new Object[] {this.portType, this.name}, new Object[] {that.portType, that.name});
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", portType, name);
        }
    }

    protected static class BlockProxy implements Serializable {

        private final byte[] data;

        private BlockProxy(byte[] data) {
            this.data = data;
        }

        protected Object readResolve() throws ObjectStreamException {
            try {
                final Unmarshaller unmarshaller = BdObjectProvider.JAXB_CONTEXT.createUnmarshaller();
                return unmarshaller.unmarshal(new ByteArrayInputStream(data));
            } catch (JAXBException x) {
                throw new IllegalStateException(x);
            }
        }
    }
}
