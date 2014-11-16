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

package org.marid.jmx;

import org.marid.xml.bind.Property;

import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement
@XmlSeeAlso({Property.class})
public class JmxAttribute implements Serializable {

    @XmlAttribute
    private final String domain;

    @XmlTransient
    private final Hashtable<String, String> properties = new Hashtable<>();

    @XmlAttribute
    private final String name;

    public JmxAttribute(ObjectName objectName, String name) {
        this.domain = objectName.getDomain();
        this.properties.putAll(objectName.getKeyPropertyList());
        this.name = name;
    }

    public JmxAttribute(ObjectName objectName, MBeanAttributeInfo attributeInfo) {
        this(objectName, attributeInfo.getName());
    }

    public JmxAttribute() {
        domain = name = null;
    }

    @XmlElementRef
    private Property[] getProperties() {
        return properties.isEmpty() ? null : properties.entrySet().stream().map(Property::new).toArray(Property[]::new);
    }

    private void setProperties(Property[] properties) {
        if (properties != null) {
            for (final Property property : properties) {
                this.properties.put(property.key, property.value);
            }
        }
    }

    public ObjectName getObjectName() {
        try {
            return ObjectName.getInstance(domain, properties);
        } catch (MalformedObjectNameException x) {
            throw new IllegalStateException(x);
        }
    }

    public String getName() {
        return name;
    }

    protected void visitToStringMap(Map<String, Object> map) {
        map.put("name", name);
        map.put("domain", domain);
        map.put("properties", properties);
    }

    @Override
    public String toString() {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        visitToStringMap(map);
        return getClass().getSimpleName() + map;
    }
}
