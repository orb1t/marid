/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.data.xml;

import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * XML map data.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
@XmlRootElement(name = "data")
public class XmlMapData {

    @XmlAttribute
    private final XmlMapType type = XmlMapType.LINKED_HASH_MAP;
    @XmlTransient
    private Map<String, Object> map;

    public XmlMapType getType() {
        return type;
    }

    public Map<String, Object> getMap() {
        return map;
    }
}
