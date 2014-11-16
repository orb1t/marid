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

package org.marid.xml.bind;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement
public class Property {

    @XmlAttribute
    public final String key;

    @XmlAttribute
    public final String value;

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Property(Map.Entry<String, String> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public Property() {
        this(null, null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Property) {
            final Property that = (Property) obj;
            return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("{%s=%s}", key, value);
    }
}
