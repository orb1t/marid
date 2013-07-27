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
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Dmitry Ovchinnikov
 */
public class ByteEntry extends AbstractEntry<Byte> {

    @XmlTransient
    private Byte value;

    public ByteEntry() {
        value = 0;
    }

    public ByteEntry(String key, Byte value) {
        super(key);
        this.value = value;
    }

    @Override
    public Byte getValue() {
        return value;
    }

    @XmlAttribute(name = "value")
    private String getStringValue() {
        return value == null ? null : value.toString();
    }

    private void setStringValue(String value) {
        this.value = Byte.decode(value);
    }
}
