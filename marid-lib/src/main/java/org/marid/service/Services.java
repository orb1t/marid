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

import org.marid.service.xml.ServiceDescriptor;
import org.marid.service.xml.ServiceEntry;
import org.marid.service.xml.ServiceList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.severe;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Services {

    private static final Logger LOG = Logger.getLogger(Services.class.getName());
    private static final Map<String, Service> SRV_MAP = new LinkedHashMap<>();

    public static void load(URL url) {
        ServiceList serviceList;
        try {
            JAXBContext context = JAXBContext.newInstance(ServiceList.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            serviceList = (ServiceList) unmarshaller.unmarshal(url);
        } catch (Exception x) {
            severe(LOG, "Unable to load services", x);
            return;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = Services.class.getClassLoader();
        }
        for (ServiceEntry entry : serviceList.services) {
            try {
                Service service = entry.service(loader);
                SRV_MAP.put(service.id(), service);
            } catch (Exception x) {
                warning(LOG, "Unable to load service {0}", x, entry);
            }
        }
    }

    public static Service getServiceById(String id) {
        return SRV_MAP.get(id);
    }

    public static Set<String> getServiceIds() {
        return Collections.unmodifiableSet(SRV_MAP.keySet());
    }

    public static Service getServiceFor(String type, ServiceDescriptor descriptor) {
        return SRV_MAP.get(descriptor.getServiceId(type));
    }
}
