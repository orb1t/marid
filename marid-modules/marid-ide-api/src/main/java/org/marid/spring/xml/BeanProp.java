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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.spring.util.InvalidationUtils;

import javax.xml.bind.annotation.*;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({DCollection.class})
public class BeanProp extends AbstractData<BeanProp> {

    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final ObjectProperty<DElement<?>> data = new SimpleObjectProperty<>(this, "data");

    public BeanProp() {
        InvalidationUtils.installChangeListener(data);
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @XmlAnyElement(lax = true)
    public DElement<?> getData() {
        return data.get();
    }

    public void setData(DElement<?> data) {
        this.data.set(data);
    }

    public boolean isEmpty() {
        return data.isNull().get();
    }

    public Observable[] observables() {
        return new Observable[] {name, data};
    }
}
