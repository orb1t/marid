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
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "int-array-entry")
public class IntArrayEntry implements Entry<int[]> {

    @XmlAttribute
    private String key;

    @XmlTransient
    private int[] value;

    public IntArrayEntry() {
        key = "";
        value = new int[0];
    }

    public IntArrayEntry(String key, int[] value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public int[] getValue() {
        return value;
    }

    @XmlValue
    private String getStringValue() {
        if (value.length == 0) {
            return "";
        } else if (value.length == 1) {
            return Integer.toString(value[0]);
        } else {
            StringBuilder sb = new StringBuilder(Integer.toString(value[0]));
            for (int i = 1; i < value.length; i++) {
                sb.append(' ');
                sb.append(value[i]);
            }
            return sb.toString();
        }
    }

    private void setStringValue(String value) {
        value = value.trim();
        if (value.isEmpty()) {
            this.value = new int[0];
        }
        String[] ps = value.split("\\s");
        this.value = new int[ps.length];
        for (int i = 0; i < ps.length; i++) {
            this.value[i] = Integer.decode(ps[i].trim());
        }
    }

    @Override
    public int hashCode() {
        return DataUtil.hashCode(this);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return DataUtil.equals(this, obj);
    }

    @Override
    public String toString() {
        return DataUtil.toString(this);
    }
}
