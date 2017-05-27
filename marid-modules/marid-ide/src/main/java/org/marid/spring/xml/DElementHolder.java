/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

import javafx.beans.value.ObservableStringValue;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.beans.OProp;
import org.springframework.core.ResolvableType;

import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public interface DElementHolder {

    DElement getData();

    String getName();

    OProp<DElement> dataProperty();

    ObservableStringValue nameProperty();

    default void refresh(ResolvableType type, ProjectProfile profile, Set<Object> passed) {
        if (!passed.add(this)) {
            return;
        }
        if (getData() != null) {
            getData().resolvableType.set(type);
            getData().refresh(profile, passed);
        }
    }
}
