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

import org.marid.jfx.beans.OOList;
import org.marid.jfx.beans.OString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.marid.misc.Iterables.nodes;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class DProps extends DElement {

    public final OString valueType = new OString("value-type");
    public final OOList<DPropEntry> entries = new OOList<>();

    public DProps() {
        valueType.addListener(this::fireInvalidate);
        entries.addListener(this::fireInvalidate);
    }

    public String getValueType() {
        return valueType.get() == null || valueType.get().isEmpty() ? null : valueType.get();
    }

    public void setValueType(String valueType) {
        this.valueType.set(valueType);
    }

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

    @Override
    public void loadFrom(Document document, Element element) {
        of(element.getAttribute("value-type")).filter(s -> !s.isEmpty()).ifPresent(valueType::set);
        nodes(element, Element.class).filter(e -> "entry".equals(e.getTagName())).forEach(e -> {
            final DPropEntry entry = new DPropEntry();
            entry.loadFrom(document, e);
            entries.add(entry);
        });
    }

    @Override
    public void writeTo(Document document, Element element) {
        ofNullable(valueType.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("value-type", e));
        entries.stream().filter(e -> !e.isEmpty()).forEach(e -> {
            final Element el = document.createElement("entry");
            e.writeTo(document, el);
            element.appendChild(el);
        });
    }
}
