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
 * @author Dmitry Ovchinnikov
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({DCollection.class})
public class BeanProp extends AbstractData<BeanProp> {

    public final FxString name = new FxString(null, "name");
    public final FxObject<DElement<?>> data = new FxObject<>(null, "data");

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
        return data.get() == null;
    }

    public FxObservable[] observables() {
        final DElement<?> e = data.get();
        return e == null
                ? new FxObservable[] {name, data}
                : concat(observableStream(), of(e.observables())).toArray(FxObservable[]::new);
    }

    @Override
    public Stream<FxObservable> observableStream() {
        return of(name, data);
    }

    @Override
    public String toString() {
        return String.format("Prop(%s,%s)", getName(), getData());
    }
}
