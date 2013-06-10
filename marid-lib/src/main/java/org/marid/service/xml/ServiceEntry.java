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

package org.marid.service.xml;

import org.marid.service.Service;
import org.marid.service.ServiceSupplier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Constructor;
import java.net.URL;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement
public class ServiceEntry {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String type;

    @XmlAttribute
    private String className;

    @XmlAttribute
    private boolean supplier;

    @XmlAttribute
    private String descriptor;

    public String getId() {
        return id;
    }

    public ServiceEntry setId(String id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public ServiceEntry setType(String type) {
        this.type = type;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public ServiceEntry setClassName(String className) {
        this.className = className;
        return this;
    }

    public boolean isSupplier() {
        return supplier;
    }

    public ServiceEntry setSupplier(boolean supplier) {
        this.supplier = supplier;
        return this;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public ServiceEntry setDescriptor(String descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    public String className() {
        if (className == null) {
            throw new NullPointerException("Class name is null: " + this);
        } else {
            return className;
        }
    }

    public String type() {
        if (type != null) {
            return type;
        } else {
            String[] parts = className().trim().split("[.]");
            if (parts.length < 2) {
                throw new IllegalStateException("Illegal class name: " + this);
            } else {
                return parts[parts.length - 2];
            }
        }
    }

    public String id() {
        return id != null ? id : type();
    }

    public String descriptor() {
        if (descriptor == null) {
            throw new NullPointerException("Descriptor is null: " + this);
        } else {
            return descriptor;
        }
    }

    public Service service(ClassLoader loader) throws Exception {
        Class<?> k = Class.forName(className(), true, loader);
        if (supplier) {
            ServiceSupplier serviceSupplier = (ServiceSupplier) k.newInstance();
            return serviceSupplier.newInstance(id(), type(), serviceDescriptor(loader));
        } else {
            Constructor c = k.getConstructor(String.class, String.class, ServiceDescriptor.class);
            return (Service) c.newInstance(id(), type(), serviceDescriptor(loader));
        }
    }

    public ServiceDescriptor serviceDescriptor(ClassLoader loader) throws Exception {
        String dsc = descriptor();
        URL url = dsc.startsWith("/") ? loader.getResource(dsc) : new URL(dsc);
        JAXBContext context = JAXBContext.newInstance(ServiceDescriptor.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ServiceDescriptor) unmarshaller.unmarshal(url);
    }
}
