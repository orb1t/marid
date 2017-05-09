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

import org.marid.jfx.beans.FxObject;
import org.marid.jfx.beans.FxObservable;
import org.marid.jfx.beans.FxString;

import javax.xml.bind.annotation.*;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.NONE)
public class DMapEntry extends AbstractData<DMapEntry> {

    public final FxString key = new FxString(null, "key");
    public final FxObject<DElement<?>> value = new FxObject<>(null, "value");

    @XmlAttribute
    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    @XmlAnyElement(lax = true)
    public DElement<?> getValue() {
        return value.get();
    }

    public void setValue(DElement<?> value) {
        this.value.set(value);
    }

    public FxObservable[] observables() {
        final DElement<?> e = value.get();
        return e == null
                ? new FxObservable[] {key, value}
                : concat(observableStream(), of(e.observables())).toArray(FxObservable[]::new);
    }

    @Override
    public Stream<FxObservable> observableStream() {
        return of(key, value);
    }

    public boolean isEmpty() {
        if (key.get() == null || key.get().isEmpty()) {
            return true;
        }
        if (value.get() == null) {
            return true;
        }
        return false;
    }

    @Override
    public Stream<? extends AbstractData<?>> stream() {
        final DElement<?> element = value.get();
        return element == null ? Stream.empty() : of(element);
    }
}
