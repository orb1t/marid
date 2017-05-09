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
 * @since 0.8
 */
@XmlRootElement(name = "map")
@XmlSeeAlso({DMapEntry.class})
@XmlAccessorType(XmlAccessType.NONE)
public class DMap extends DElement<DMap> {

    public final FxString keyType = new FxString(null, "key-type");
    public final FxString valueType = new FxString(null, "value-type");
    public final FxList<DMapEntry> entries = new FxList<>(DMapEntry::observables);

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public FxObservable[] observables() {
        return new FxObservable[] {keyType, valueType, entries};
    }

    @Override
    public Stream<FxObservable> observableStream() {
        return Stream.of(observables());
    }

    @XmlAttribute(name = "key-type")
    public String getKeyType() {
        return keyType.get();
    }

    public void setKeyType(String keyType) {
        this.keyType.set(keyType);
    }

    @XmlAttribute(name = "value-type")
    public String getValueType() {
        return valueType.get();
    }

    public void setValueType(String valueType) {
        this.valueType.set(valueType);
    }

    @XmlElement(name = "entry")
    public DMapEntry[] getEntries() {
        return entries.stream().filter(e -> !e.isEmpty()).toArray(DMapEntry[]::new);
    }

    public void setEntries(DMapEntry[] entries) {
        this.entries.setAll(entries);
    }

    @Override
    public String toString() {
        return String.format("Map<%s,%s>(%d)", keyType.get(), valueType.get(), entries.size());
    }
}
