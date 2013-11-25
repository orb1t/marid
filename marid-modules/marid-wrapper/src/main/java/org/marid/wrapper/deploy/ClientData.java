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

package org.marid.wrapper.deploy;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "client")
public class ClientData {

    @XmlElement(name = "jvm-properties")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    private final Map<String, String> jvmProperties = new HashMap<>();

    @XmlElement(name = "environment")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    private final Map<String, String> environment = new HashMap<>();

    @XmlElement
    private String user = "guest";

    @XmlElement
    private byte[] password = new byte[0];

    public Map<String, String> getJvmProperties() {
        return Collections.unmodifiableMap(jvmProperties);
    }

    public Map<String, String> getEnvironment() {
        return Collections.unmodifiableMap(environment);
    }

    public ClientData addJvmProperty(String key, String value) {
        jvmProperties.put(key, value);
        return this;
    }

    public ClientData addJvmProperties(Properties properties) {
        for (final String key : properties.stringPropertyNames()) {
            jvmProperties.put(key, properties.getProperty(key));
        }
        return this;
    }

    public ClientData addJvmProperties() {
        return addJvmProperties(System.getProperties());
    }

    public ClientData addEnvProperty(String key, String value) {
        environment.put(key, value);
        return this;
    }

    public ClientData addEnvProperties(Map<String, String> env) {
        for (Map.Entry<String, String> e : env.entrySet()) {
            environment.put(e.getKey(), e.getValue());
        }
        return this;
    }

    public ClientData addEnvProperties() {
        return addEnvProperties(System.getenv());
    }

    @XmlTransient
    public String getUser() {
        return user;
    }

    public ClientData setUser(String user) {
        this.user = user;
        return this;
    }

    @XmlTransient
    public byte[] getPassword() {
        return password;
    }

    public ClientData setPassword(byte[] password) {
        this.password = password;
        return this;
    }

    private static class PropertiesEntry {

        private PropertiesEntry() {
        }

        private PropertiesEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @XmlAttribute
        private String key;

        @XmlAttribute
        private String value;
    }

    private static class PropertiesEntries {

        @XmlElement(name = "property")
        private PropertiesEntry[] entries;

        private PropertiesEntries() {
            entries = new PropertiesEntry[0];
        }

        private PropertiesEntries(Map<String, String> properties) {
            entries = new PropertiesEntry[properties.size()];
            int i = 0;
            for (final Entry<String, String> e : properties.entrySet()) {
                entries[i++] = new PropertiesEntry(e.getKey(), e.getValue());
            }
        }

        private Map<String, String> toProperties() {
            final Map<String, String> properties = new HashMap<>(entries.length);
            for (final PropertiesEntry entry : entries) {
                properties.put(entry.key, entry.value);
            }
            return properties;
        }
    }

    private static class PropertiesAdapter extends XmlAdapter<PropertiesEntries, Map<String, String>> {

        @Override
        public Map<String, String> unmarshal(PropertiesEntries v) throws Exception {
            return v.toProperties();
        }

        @Override
        public PropertiesEntries marshal(Map<String, String> v) throws Exception {
            return new PropertiesEntries(v);
        }
    }
}
