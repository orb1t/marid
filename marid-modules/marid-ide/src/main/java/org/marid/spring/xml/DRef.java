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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "ref")
public final class DRef extends DElement<DRef> {

    public final FxString ref = new FxString(null, "value");

    public DRef() {
    }

    public DRef(String value) {
        ref.set(value);
    }

    @XmlAttribute(name = "bean")
    public String getBean() {
        return isEmpty() ? null : ref.get();
    }

    public void setBean(String bean) {
        ref.set(bean);
    }

    @Override
    public boolean isEmpty() {
        return ref.get() == null || ref.get().isEmpty();
    }

    @Override
    public FxObservable[] observables() {
        return new FxObservable[] {ref};
    }

    @Override
    public Stream<FxObservable> observableStream() {
        return Stream.of(ref);
    }

    @Override
    public Stream<? extends AbstractData<?>> stream() {
        return Stream.empty();
    }

    @Override
    public String toString() {
        return getBean();
    }
}
