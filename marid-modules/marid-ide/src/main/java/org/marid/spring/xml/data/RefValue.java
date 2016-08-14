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

package org.marid.spring.xml.data;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.collection.DCollection;
import org.marid.spring.xml.data.collection.DElement;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({DCollection.class})
public abstract class RefValue<T extends RefValue<T>> implements AbstractData<T> {

    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty ref = new SimpleStringProperty(this, "ref");
    public final StringProperty type = new SimpleStringProperty(this, "type");
    public final StringProperty value = new SimpleStringProperty(this, "value");

    public final ObjectProperty<DElement<?>> data = new SimpleObjectProperty<>(this, "data");

    @XmlAttribute(name = "name")
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @XmlAttribute(name = "ref")
    public String getRef() {
        return ref.isEmpty().get() ? null : ref.get();
    }

    public void setRef(String ref) {
        this.ref.set(ref);
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

    @XmlAttribute(name = "value")
    public String getValue() {
        return value.isEmpty().get() ? null : value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public boolean isEmpty() {
        return data.isNull().get() && value.isEmpty().get() && ref.isEmpty().get();
    }

    public abstract Optional<? extends Type> getType(ProjectProfile profile);
}
