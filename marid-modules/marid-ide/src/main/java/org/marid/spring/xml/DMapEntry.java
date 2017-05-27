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

import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.beans.OOProp;
import org.marid.jfx.beans.OProp;
import org.marid.jfx.beans.OString;
import org.springframework.core.ResolvableType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;
import java.util.Set;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.marid.misc.Iterables.nodes;
import static org.marid.spring.xml.DElement.read;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class DMapEntry extends AbstractData {

    public final OProp<ResolvableType> keyType = new OProp<>("keyType", ResolvableType.forClass(String.class));
    public final OProp<ResolvableType> valueType = new OProp<>("valueType", ResolvableType.NONE);

    public final OString key = new OString("key");
    public final OOProp<DElement> value = new OOProp<>("value");

    public DMapEntry() {
        key.addListener(this::fireInvalidate);
        value.addListener(this::fireInvalidate);
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public DElement getValue() {
        return value.get();
    }

    public void setValue(DElement value) {
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
        of(element.getAttribute("key")).filter(f -> !f.isEmpty()).ifPresent(key::set);
        nodes(element, Element.class).map(e -> read(document, e)).filter(Objects::nonNull).forEach(value::set);
    }

    @Override
    public void writeTo(Document document, Element element) {
        ofNullable(key.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("key", e));
        ofNullable(value.get()).filter(e -> !e.isEmpty()).ifPresent(value::set);
    }

    @Override
    protected void refresh(ProjectProfile profile, Set<Object> passed) {
        if (!passed.add(this)) {
            return;
        }
        if (keyType.get() == ResolvableType.NONE) {
            keyType.set(ResolvableType.forClass(String.class));
        }
    }
}
