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
@XmlSeeAlso({DCollection.class})
@XmlAccessorType(XmlAccessType.NONE)
public class BeanArg extends AbstractData<BeanArg> {

    public final FxString name = new FxString(null, "name");
    public final FxString type = new FxString(null, "type");
    public final FxObject<DElement<?>> data = new FxObject<>(null, "data");

    @XmlAttribute(name = "name")
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @XmlAttribute(name = "type")
    public String getType() {
        return type.get() == null || type.get().isEmpty() ? null : type.get();
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
        return data.get() == null;
    }

    public FxObservable[] observables() {
        final DElement<?> e = data.get();
        return e == null
                ? new FxObservable[] {name, type, data}
                : concat(observableStream(), of(e.observables())).toArray(FxObservable[]::new);
    }

    @Override
    public Stream<FxObservable> observableStream() {
        return of(name, type, data);
    }

    @Override
    public Stream<? extends AbstractData<?>> stream() {
        final DElement<?> element = data.get();
        return element == null ? Stream.empty() : of(element);
    }

    @Override
    public String toString() {
        return String.format("Arg(%s,%s,%s)", getName(), getType(), getData());
    }
}
