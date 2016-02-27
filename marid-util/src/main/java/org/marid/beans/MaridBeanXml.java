/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.beans;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "bean")
public class MaridBeanXml {

    @XmlAttribute(name = "icon")
    public String icon;

    @XmlAttribute(name = "description")
    public String description;

    @XmlAttribute(name = "text")
    public String text;

    @XmlAttribute(name = "help")
    public String help;

    @XmlAttribute(name = "type", required = true)
    public String type;

    public MaridBeanXml() {
    }

    public MaridBeanXml(MaridBean maridBean) {
        text = maridBean.text().isEmpty() ? null : maridBean.text();
        icon = maridBean.icon().isEmpty() ? null : maridBean.icon();
        help = maridBean.help().isEmpty() ? null : maridBean.help();
        description = maridBean.description().isEmpty() ? null : maridBean.description();
    }

    @Override
    public String toString() {
        final Map<String, Object> map = new LinkedHashMap<>();
        if (type != null) {
            map.put("type", type);
        }
        if (text != null) {
            map.put("text", text);
        }
        if (icon != null) {
            map.put("icon", icon);
        }
        if (help != null) {
            map.put("help", help);
        }
        if (description != null) {
            map.put("description", description);
        }
        return getClass().getSimpleName() + map;
    }
}
