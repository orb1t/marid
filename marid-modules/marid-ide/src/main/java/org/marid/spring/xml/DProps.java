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

import org.marid.jfx.beans.FxList;
import org.marid.jfx.beans.FxObservable;
import org.marid.jfx.beans.FxString;

import javax.xml.bind.annotation.*;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement(name = "props")
@XmlSeeAlso({DPropEntry.class})
@XmlAccessorType(XmlAccessType.NONE)
public final class DProps extends DElement<DProps> {

    public final FxString valueType = new FxString(null, "value-type");
    public final FxList<DPropEntry> entries = new FxList<>(DPropEntry::observables);

    @XmlAttribute(name = "value-type")
    public String getValueType() {
        return valueType.get() == null || valueType.get().isEmpty() ? null : valueType.get();
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
    public FxObservable[] observables() {
        return new FxObservable[] {valueType, entries};
    }

    @Override
    public Stream<FxObservable> observableStream() {
        return Stream.of(observables());
    }

    @Override
    public String toString() {
        return String.format("Props(%d)", entries.size());
    }
}
