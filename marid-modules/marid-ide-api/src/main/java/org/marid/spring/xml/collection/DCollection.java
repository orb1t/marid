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

package org.marid.spring.xml.collection;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.marid.spring.xml.props.DProps;
import org.marid.spring.xml.ref.DRef;
import org.marid.util.MaridCollections;

import javax.xml.bind.annotation.*;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlSeeAlso({DList.class, DArray.class, DValue.class, DProps.class, DRef.class})
@XmlAccessorType(XmlAccessType.NONE)
public abstract class DCollection<T extends DCollection<T>> extends DElement<T> {

    public final StringProperty valueType = new SimpleStringProperty(this, "value-type");
    public final ObservableList<DElement<?>> elements = MaridCollections.list();

    public DCollection() {
        final InvalidationListener invalidationListener = o -> invalidate();
        valueType.addListener(invalidationListener);
        elements.addListener(invalidationListener);
    }

    @XmlAttribute(name = "value-type")
    public String getValueType() {
        return valueType.isEmpty().get() ? null : valueType.get();
    }

    public void setValueType(String valueType) {
        this.valueType.set(valueType);
    }

    @XmlAnyElement(lax = true)
    public DElement<?>[] getElements() {
        return elements.stream().filter(e -> !e.isEmpty()).toArray(DElement[]::new);
    }

    public void setElements(DElement<?>[] elements) {
        this.elements.addAll(elements);
    }

    @Override
    public String toString() {
        final String className = getClass().getSimpleName().substring(1);
        return className + "(" + elements.size() + ")";
    }
}
