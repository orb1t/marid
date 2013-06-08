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
import org.marid.service.xml.ServiceList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.URL;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.severe;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class Services {

    private static final Logger LOG = Logger.getLogger(Services.class.getName());
    private static final JAXBContext SRV_LIST_CONTEXT = getContext(ServiceList.class);
    private static final JAXBContext SRV_DESCRIPTOR_CONTEXT = getContext(ServiceDescriptor.class);

    public static void load(URL url) {
        ServiceList serviceList;
        try {
            Unmarshaller unmarshaller = SRV_LIST_CONTEXT.createUnmarshaller();
            serviceList = (ServiceList) unmarshaller.unmarshal(url);
        } catch (Exception x) {
            severe(LOG, "Unable to load services", x);
            return;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = Services.class.getClassLoader();
        }
        for (ServiceList.ServiceEntry entry : serviceList.services) {
            if (entry.descriptor == null) {
                warning(LOG, "Null descriptor in {0}", entry);
                continue;
            }
            if (entry.supplierClassName == null && entry.serviceClassName == null) {
                warning(LOG, "Null class name in {0}", entry);
            }
            String type;
            if (entry.type == null) {
                String className = entry.serviceClassName != null ? entry.serviceClassName :
                                entry.supplierClassName != null ? entry.supplierClassName : null;
            }
        }
    }

    private static JAXBContext getContext(Class<?> bindClass) {
        try {
            return JAXBContext.newInstance(bindClass);
        } catch (JAXBException x) {
            severe(LOG, "Unable to get context from {0}", x, bindClass);
            return null;
        }
    }
}
