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
import org.marid.jfx.beans.OString;
import org.springframework.core.ResolvableType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.Set;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.marid.misc.Iterables.nodes;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class DMap extends DElement {

    public final OString keyType = new OString("key-type");
    public final OString valueType = new OString("value-type");
    public final OOList<DMapEntry> entries = new OOList<>();

    public DMap() {
        keyType.addListener(this::fireInvalidate);
        valueType.addListener(this::fireInvalidate);
        entries.addListener(this::fireInvalidate);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public String getKeyType() {
        return keyType.get();
    }

    public void setKeyType(String keyType) {
        this.keyType.set(keyType);
    }

    public String getValueType() {
        return valueType.get();
    }

    public void setValueType(String valueType) {
        this.valueType.set(valueType);
    }

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

    @Override
    public void loadFrom(Document document, Element element) {
        of(element.getAttribute("key-type")).filter(s -> !s.isEmpty()).ifPresent(keyType::set);
        of(element.getAttribute("value-type")).filter(s -> !s.isEmpty()).ifPresent(valueType::set);
        nodes(element, Element.class).filter(e -> "entry".equals(e.getTagName())).forEach(e -> {
            final DMapEntry entry = new DMapEntry();
            entry.loadFrom(document, e);
            entries.add(entry);
        });
    }

    @Override
    public void writeTo(Document document, Element element) {
        ofNullable(keyType.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("key-type", e));
        ofNullable(valueType.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("value-type", e));
        entries.stream().filter(e -> !e.isEmpty()).forEach(e -> {
            final Element el = document.createElement("entry");
            e.writeTo(document, el);
            element.appendChild(el);
        });
    }

    @Override
    protected void refresh(ProjectProfile profile, Set<Object> passed) {
        if (!passed.add(this)) {
            return;
        }
        if (resolvableType.get() == ResolvableType.NONE) {
            if (valueType.get() != null && !valueType.get().isEmpty()) {
                final Class<?> k = profile.getClass(keyType.get()).orElse(String.class);
                final Class<?> v = profile.getClass(valueType.get()).orElse(Object.class);
                resolvableType.set(ResolvableType.forClassWithGenerics(Map.class, k, v));
            }
        } else {
            if (!ResolvableType.forClass(Map.class).isAssignableFrom(resolvableType.get())) {
                return;
            }
            final ResolvableType genericType = ResolvableType.forType(Map.class, resolvableType.get());
            final ResolvableType k = genericType.getGeneric(0);
            final ResolvableType v = genericType.getGeneric(1);
            entries.forEach(e -> {
                if (k != null && k != ResolvableType.NONE) {
                    e.keyType.set(k);
                }
                if (v != null && v != ResolvableType.NONE) {
                    e.valueType.set(v);
                }
                e.refresh(profile, passed);
            });
        }
    }
}
