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

package org.marid.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "bool-entry")
public class BooleanEntry extends AbstractEntry<Boolean> {

    @XmlTransient
    private Boolean value;

    public BooleanEntry() {
        value = false;
    }

    public BooleanEntry(String key, Boolean value) {
        super(key);
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @XmlAttribute(name = "value")
    String getStringValue() {
        return Boolean.TRUE.equals(value) ? "1" : "0";
    }

    void setStringValue(String value) {
        this.value = "1".equals(value)
                || "true".equalsIgnoreCase(value)
                || "on".equalsIgnoreCase(value);
    }
}
