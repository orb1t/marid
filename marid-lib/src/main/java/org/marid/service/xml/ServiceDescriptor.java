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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name="descriptor")
public class ServiceDescriptor {

    @XmlElement
    private String delegateId;

    @XmlElementWrapper(name = "services")
    @XmlElement(name = "id")
    private Set<String> serviceIds = new TreeSet<>();

    public String getDelegateId() {
        return delegateId;
    }

    public ServiceDescriptor setDelegateId(String delegateId) {
        this.delegateId = delegateId;
        return this;
    }

    public ServiceDescriptor addServiceId(String serviceId) {
        serviceIds.add(serviceId);
        return this;
    }

    public ServiceDescriptor removeServiceId(String serviceId) {
        serviceIds.remove(serviceId);
        return this;
    }

    public ServiceDescriptor clearServiceIds() {
        serviceIds.clear();
        return this;
    }

    public ServiceDescriptor addServiceIds(Set<String> ids) {
        serviceIds.addAll(ids);
        return this;
    }

    public Set<String> getServiceIds() {
        return serviceIds;
    }
}
