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

import org.marid.l10n.Localized;
import org.marid.service.data.Request;
import org.marid.service.data.Response;
import org.marid.service.xml.ServiceDescriptor;
import org.marid.typecast.Configurable;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Service extends Configurable, ThreadFactory, Localized {

    public static final Registry REGISTRY = new Registry();

    public ServiceDescriptor descriptor();

    public String name();

    public String id();

    public String type();

    public String version();

    public String label();

    public void start() throws Exception;

    public void stop() throws Exception;

    public boolean running();

    public ThreadGroup threadGroup();

    public Service getService(String type);

    public <T extends Response> Future<T> send(Request<T> message);

    public Transaction transaction(Map<String, Object> params);

    public static class Registry {

        private final Map<String, Service> serviceMap = new LinkedHashMap<>();
        private final Map<Service, String> reverseMap = new IdentityHashMap<>();
        private final Map<String, Map<String, Service>> typeIdMap = new TreeMap<>();

        void register(String id, String type, Service service) {
            serviceMap.put(id, service);
            reverseMap.put(service, id);
            Map<String, Service> idMap = typeIdMap.get(type);
            if (idMap == null) {
                typeIdMap.put(type, idMap = new TreeMap<>());
            }
            idMap.put(id, service);
        }

        public Map<String, Service> getServiceMap() {
            return Collections.unmodifiableMap(serviceMap);
        }

        public Map<Service, String> getReverseMap() {
            return Collections.unmodifiableMap(reverseMap);
        }

        public Map<String, Map<String, Service>> getTypeIdMap() {
            return Collections.unmodifiableMap(typeIdMap);
        }

        public String id(Service service) {
            return reverseMap.get(service);
        }
    }

    public static class Event extends EventObject {

        private static final long serialVersionUID = 4108613835844220191L;
        private final String id;
        private final Service service;

        public Event(Registry source, String id, Service service) {
            super(source);
            this.id = id;
            this.service = service;
        }

        public String getId() {
            return id;
        }

        public Service getService() {
            return service;
        }

        @Override
        public Registry getSource() {
            return (Registry) super.getSource();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + id + "," + service + ")";
        }
    }
}
