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

import org.marid.ide.swing.mbean.node.AttributeNode;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement
public class IdeJmxAttribute extends JmxAttribute {

    @XmlAttribute
    private final String connection;

    public IdeJmxAttribute(String connection, ObjectName objectName, String name) {
        super(objectName, name);
        this.connection = connection;
    }

    public IdeJmxAttribute(String connection, ObjectName objectName, MBeanAttributeInfo attributeInfo) {
        super(objectName, attributeInfo);
        this.connection = connection;
    }

    public IdeJmxAttribute(String connection, AttributeNode attributeNode) {
        super(attributeNode.getParent().getParent().getObjectName(), attributeNode.getAttributeInfo());
        this.connection = connection;
    }

    private IdeJmxAttribute() {
        this(null, null, (String) null);
    }

    public String getConnection() {
        return connection;
    }

    @Override
    protected void visitToStringMap(Map<String, Object> map) {
        map.put("connection", connection);
        super.visitToStringMap(map);
    }
}
