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

import javax.xml.bind.annotation.*;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@XmlSeeAlso({DCollection.class})
@XmlAccessorType(XmlAccessType.NONE)
public class BeanArg extends AbstractData<BeanArg> {

    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty type = new SimpleStringProperty(this, "type");
    public final ObjectProperty<DElement<?>> data = new SimpleObjectProperty<>(this, "data");

    public BeanArg() {
        data.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Stream.of(newValue.observables()).forEach(o -> o.addListener(this::onInvalidate));
            }
            if (oldValue != null) {
                Stream.of(oldValue.observables()).forEach(o -> o.removeListener(this::onInvalidate));
            }
        });
    }

    private void onInvalidate(Observable observable) {
        final DElement<?> element = data.get();
        data.set(null);
        data.set(element);
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @XmlAttribute(name = "type")
    public String getType() {
        return type.isEmpty().get() ? null : type.get();
    }

    public void setType(String type) {
        this.type.set(type);
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
        return new Observable[] {name, type, data};
    }
}
