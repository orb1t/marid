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
import org.marid.jfx.beans.OString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Set;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * @author Dmitry Ovchinnikov
 */
public final class DRef extends DElement {

    public final OString ref = new OString("value");

    public DRef() {
        this(null);
    }

    public DRef(String value) {
        ref.set(value);
        ref.addListener(this::fireInvalidate);
    }

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
    public String toString() {
        return getBean();
    }

    @Override
    public void loadFrom(Document document, Element element) {
        of(element.getAttribute("bean")).filter(s -> !s.isEmpty()).ifPresent(ref::set);
    }

    @Override
    public void writeTo(Document document, Element element) {
        ofNullable(ref.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("bean", e));
    }

    @Override
    protected void refresh(ProjectProfile profile, Set<Object> passed) {
    }
}
