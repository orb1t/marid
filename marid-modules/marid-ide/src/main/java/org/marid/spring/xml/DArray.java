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
import org.springframework.core.ResolvableType;

import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public final class DArray extends DCollection {

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    protected void refresh(ProjectProfile profile, Set<Object> passed) {
        if (!passed.add(this)) {
            return;
        }
        if (!resolvableType.get().isArray()) {
            return;
        }
        final ResolvableType componentType = resolvableType.get().getComponentType();
        if (componentType == null || componentType == ResolvableType.NONE) {
            return;
        }
        elements.forEach(e -> {
            if (e != null) {
                e.resolvableType.set(componentType);
                e.refresh(profile, passed);
            }
        });
    }
}
