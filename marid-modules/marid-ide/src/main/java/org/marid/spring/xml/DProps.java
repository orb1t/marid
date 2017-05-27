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
import org.marid.jfx.beans.OOList;
import org.springframework.core.ResolvableType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Properties;
import java.util.Set;

import static org.marid.misc.Iterables.nodes;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class DProps extends DElement {

    public final OOList<DPropEntry> entries = new OOList<>();

    public DProps() {
        entries.addListener(this::fireInvalidate);
        resolvableType.set(ResolvableType.forClass(Properties.class));
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
        nodes(element, Element.class).filter(e -> "entry".equals(e.getTagName())).forEach(e -> {
            final DPropEntry entry = new DPropEntry();
            entry.loadFrom(document, e);
            entries.add(entry);
        });
    }

    @Override
    public void writeTo(Document document, Element element) {
        entries.stream().filter(e -> !e.isEmpty()).forEach(e -> {
            final Element el = document.createElement("entry");
            e.writeTo(document, el);
            element.appendChild(el);
        });
    }

    @Override
    protected void refresh(ProjectProfile profile, Set<Object> passed) {
    }
}
