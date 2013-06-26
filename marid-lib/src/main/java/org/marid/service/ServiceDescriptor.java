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

package org.marid.service;

import javax.xml.bind.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name="descriptor")
public class ServiceDescriptor {

    @XmlElement
    private String delegateId;

    @XmlTransient
    private Map<String, String> serviceMap = new HashMap<>();

    @XmlElementWrapper(name="services")
    @XmlElement(name="service")
    private TypeIdEntry[] getEntries() {
        TypeIdEntry[] entries = new TypeIdEntry[serviceMap.size()];
        int i = 0;
        for (Map.Entry<String, String> e : serviceMap.entrySet()) {
            entries[i++] = new TypeIdEntry(e.getKey(), e.getValue());
        }
        return entries;
    }

    private void setEntries(TypeIdEntry[] entries) {
        for (TypeIdEntry e : entries) {
            serviceMap.put(e.type, e.id);
        }
    }

    public String getDelegateId() {
        return delegateId;
    }

    @XmlTransient
    public ServiceDescriptor setDelegateId(String delegateId) {
        this.delegateId = delegateId;
        return this;
    }

    public ServiceDescriptor addService(String type, String id) {
        serviceMap.put(type, id);
        return this;
    }

    public String getServiceId(String type) {
        return serviceMap.get(type);
    }

    public ServiceDescriptor removeService(String serviceId) {
        serviceMap.remove(serviceId);
        return this;
    }

    public ServiceDescriptor clearServices() {
        serviceMap.clear();
        return this;
    }

    public Map<String, String> getServiceMap() {
        return serviceMap;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ServiceDescriptor)) {
            return false;
        } else {
            ServiceDescriptor that = (ServiceDescriptor) o;
            Object[] a = {this.delegateId, this.serviceMap};
            Object[] b = {that.delegateId, that.serviceMap};
            return Arrays.equals(a, b);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegateId, serviceMap);
    }

    @Override
    public String toString() {
        return "ServiceDescriptor{" +
                "delegateId='" + delegateId + '\'' +
                ", serviceMap=" + serviceMap + '}';
    }

    static class TypeIdEntry {

        @XmlAttribute
        String type;

        @XmlAttribute
        String id;

        TypeIdEntry(String type, String id) {
            this.type = type;
            this.id = id;
        }

        TypeIdEntry() {
        }
    }
}
