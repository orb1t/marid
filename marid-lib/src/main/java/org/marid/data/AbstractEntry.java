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
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlSeeAlso({
        MapEntry.class,
        IntEntry.class,
        LongEntry.class,
        BooleanEntry.class,
        DoubleEntry.class,
        FloatEntry.class,
        IntArrayEntry.class
})
public abstract class AbstractEntry<T> implements Entry<T> {

    @XmlAttribute
    private String key;

    public AbstractEntry() {
        key = "";
    }

    public AbstractEntry(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return DataUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return DataUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return DataUtil.toString(this);
    }
}
