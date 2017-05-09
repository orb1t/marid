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

import org.marid.jfx.beans.FxObservable;
import org.marid.jfx.beans.FxString;

import javax.xml.bind.annotation.*;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement(name = "prop")
@XmlAccessorType(XmlAccessType.NONE)
public final class DPropEntry extends AbstractData<DPropEntry> {

    public final FxString key = new FxString(null, "key");
    public final FxString value = new FxString(null, "value");

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
        if (key.get() == null || key.get().isEmpty()) {
            return true;
        }
        if (value.get() == null || value.get().isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public FxObservable[] observables() {
        return new FxObservable[] {key, value};
    }

    @Override
    public Stream<FxObservable> observableStream() {
        return Stream.of(observables());
    }

    @Override
    public Stream<? extends AbstractData<?>> stream() {
        return Stream.empty();
    }
}
