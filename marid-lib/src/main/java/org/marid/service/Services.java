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
import org.marid.service.xml.ServiceList.ServiceEntry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
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
    private static final Map<String, Service> SRV_MAP = new LinkedHashMap<>();
    private static final Map<Service, String> RSRV_MAP = new IdentityHashMap<>();
    private static final Map<String, Map<String, Service>> TISRV_MAP = new TreeMap<>();

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
            loadEntry(loader, entry);
        }
    }

    private static void loadEntry(ClassLoader loader, ServiceEntry entry) {
        if (entry.descriptor == null) {
            warning(LOG, "Null descriptor for {0}", entry);
        } else if (entry.serviceClassName != null) {
            loadDirectEntry(loader, entry);
        } else if (entry.supplierClassName != null) {
            loadSupplierEntry(loader, entry);
        } else {
            warning(LOG, "{0} class == null && supplier == null", entry);
        }
    }

    private static void loadDirectEntry(ClassLoader loader, ServiceEntry entry) {
        try {
            Class<?> serviceClass = Class.forName(entry.serviceClassName, true, loader);
            Service service = (Service) serviceClass.newInstance();
            String type = getType(service, entry);
            String id = entry.id != null ? entry.id : type;

        } catch (Exception x) {
            warning(LOG, "{0} unable to load service class", entry);
        }
    }

    private static void loadSupplierEntry(ClassLoader loader, ServiceEntry entry) {
        try {
            Class<?> serviceClass = Class.forName(entry.serviceClassName, true, loader);
            Service service = (Service) serviceClass.newInstance();
            String type = getType(service, entry);
            String id = entry.id != null ? entry.id : type;

        } catch (Exception x) {
            warning(LOG, "{0} unable to load service class", entry);
        }
    }

    private static String getType(Service service, ServiceEntry entry) {
        if (entry.type != null) {
            return entry.type;
        } else {
            Package pkg = service.getClass().getPackage();
            if (pkg == null) {
                throw new IllegalArgumentException("Null package for " + service.getClass());
            }
            String pkgName = pkg.getName();
            int pos = pkgName.lastIndexOf('.');
            if (pos >= 0) {
                return pkgName.substring(pos + 1, pkgName.length());
            } else {
                throw new IllegalArgumentException("Cannot infer type for " + service.getClass());
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
