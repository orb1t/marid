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

package org.marid.spring.xml.data.props;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.marid.jfx.util.MaridCollections;
import org.marid.spring.xml.data.collection.DElement;

import javax.xml.bind.annotation.*;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement(name = "props")
@XmlSeeAlso({DPropEntry.class})
@XmlAccessorType(XmlAccessType.NONE)
public final class DProps extends DElement<DProps> {

    public final StringProperty valueType = new SimpleStringProperty(this, "value-type");
    public final ObservableList<DPropEntry> entries = MaridCollections.list();

    public DProps() {
        valueType.addListener(this::invalidate);
        entries.addListener(this::invalidate);
    }

    @XmlAttribute(name = "value-type")
    public String getValueType() {
        return valueType.isEmpty().get() ? null : valueType.get();
    }

    public void setValueType(String valueType) {
        this.valueType.set(valueType);
    }

    @XmlElement(name = "prop")
    public DPropEntry[] getEntries() {
        return entries.stream().filter(e -> !e.isEmpty()).toArray(DPropEntry[]::new);
    }

    public void setEntries(DPropEntry[] entries) {
        this.entries.addAll(entries);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("Props(%d)", entries.size());
    }
}
