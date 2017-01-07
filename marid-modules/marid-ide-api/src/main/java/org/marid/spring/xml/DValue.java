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

package org.marid.spring.xml;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.NONE)
public final class DValue extends DElement<DValue> {

    public final StringProperty value = new SimpleStringProperty(this, "value");

    public DValue() {
    }

    public DValue(String value) {
        this.value.set(value);
    }

    @XmlValue
    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty().get();
    }

    @Override
    public Observable[] observables() {
        return new Observable[] {value};
    }

    @Override
    public String toString() {
        return getValue();
    }
}
