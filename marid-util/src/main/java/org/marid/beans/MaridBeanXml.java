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

import org.marid.function.SafeFunction;

import javax.lang.model.element.ElementKind;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @XmlAttribute(name = "kind", required = true)
    public ElementKind kind;

    @XmlAttribute(name = "parent")
    public String parent;

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
        return getClass().getSimpleName() + Stream.of(getClass().getFields())
                .map((SafeFunction<Field, Entry<String, Object>>) f -> new SimpleImmutableEntry<>(f.getName(), f.get(this)))
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (o1, o2) -> o2, TreeMap::new));
    }
}
