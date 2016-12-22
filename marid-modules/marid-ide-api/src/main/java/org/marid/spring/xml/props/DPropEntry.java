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

package org.marid.spring.xml.props;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.spring.xml.AbstractData;

import javax.xml.bind.annotation.*;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement(name = "prop")
@XmlAccessorType(XmlAccessType.NONE)
public final class DPropEntry extends AbstractData<DPropEntry> {

    public final StringProperty key = new SimpleStringProperty(this, "key");
    public final StringProperty value = new SimpleStringProperty(this, "value");

    public DPropEntry() {
        final InvalidationListener invalidationListener = o -> invalidate();
        key.addListener(invalidationListener);
        value.addListener(invalidationListener);
    }

    @XmlAttribute(name = "key")
    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    @XmlValue
    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public boolean isEmpty() {
        return key.isEmpty().get() && value.isEmpty().get();
    }
}
