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
import org.marid.jfx.beans.FxString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.marid.misc.Iterables.nodes;
import static org.marid.spring.xml.DElement.read;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.NONE)
public class DMapEntry extends AbstractData<DMapEntry> {

    public final FxString key = new FxString(null, "key");
    public final FxObject<DElement<?>> value = new FxObject<>(null, "value");

    public DMapEntry() {
        key.addListener(this::fireInvalidate);
        value.addListener(this::fireInvalidate);
    }

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
    public void loadFrom(Document document, Element element) {
        ofNullable(element.getAttribute("key")).ifPresent(key::set);
        nodes(element, Element.class).map(e -> read(document, e)).filter(Objects::nonNull).forEach(value::set);
    }

    @Override
    public void writeTo(Document document, Element element) {
        ofNullable(key.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("key", e));
        ofNullable(value.get()).filter(e -> !e.isEmpty()).ifPresent(value::set);
    }
}
