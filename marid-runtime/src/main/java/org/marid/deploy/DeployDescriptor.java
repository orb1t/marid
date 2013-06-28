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

package org.marid.deploy;

import org.marid.util.Builder;

import javax.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "deploy")
public class DeployDescriptor extends Builder {

    @XmlElement(name = "jmx")
    private JmxDescriptor jmxDescriptor = new JmxDescriptor();

    @XmlElementWrapper(name = "class-path")
    @XmlElement(name = "item")
    private LinkedList<String> classPath = new LinkedList<>();

    @XmlElementWrapper(name = "services")
    @XmlElement(name = "service")
    private LinkedList<ServiceInfo> services = new LinkedList<>();

    @XmlTransient
    public JmxDescriptor getJmxDescriptor() {
        return jmxDescriptor;
    }

    public DeployDescriptor setJmxDescriptor(JmxDescriptor jmxDescriptor) {
        this.jmxDescriptor = jmxDescriptor;
        return this;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public DeployDescriptor addClassPath(String classPath) {
        this.classPath.add(classPath);
        return this;
    }

    public DeployDescriptor clear() {
        classPath.clear();
        return this;
    }

    public static class JmxDescriptor extends Builder {

        @XmlAttribute
        private int port = 1099;

        @XmlAttribute
        private boolean enabled;

        @XmlTransient
        public boolean isEnabled() {
            return enabled;
        }

        public JmxDescriptor setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @XmlTransient
        public int getPort() {
            return port;
        }

        public JmxDescriptor setPort(int port) {
            this.port = port;
            return this;
        }
    }

    public static class ServiceInfo extends Builder {

        @XmlAttribute
        private String id;

        @XmlAttribute
        private String type;

        @XmlAttribute(name = "class")
        private String klass;

        @XmlAttribute
        private String url;

        @XmlTransient
        public String getId() {
            return id != null ? id : getType();
        }

        public ServiceInfo setId(String id) {
            this.id = id;
            return this;
        }

        @XmlTransient
        public String getType() {
            if (type == null) {
                if (getKlass().indexOf('.') < 0) {
                    return klass;
                } else {
                    String[] parts = klass.split("[.]");
                    return parts[parts.length - 2];
                }
            } else {
                return type;
            }
        }

        public ServiceInfo setType(String type) {
            this.type = type;
            return this;
        }

        @XmlTransient
        public String getKlass() {
            if (klass == null) {
                throw new IllegalStateException("Class cannot be null: " + this);
            } else {
                return klass;
            }
        }

        public ServiceInfo setKlass(String klass) {
            this.klass = klass;
            return this;
        }

        @XmlTransient
        public String getUrl() {
            return url;
        }

        public ServiceInfo setUrl(String url) {
            this.url = url;
            return this;
        }
    }

    public static class HttpWrapperInfo extends Builder {

        @XmlAttribute
        private String id;

        @XmlAttribute
        private String delegateId;
    }
}
