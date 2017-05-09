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

package org.marid.dependant.beantree.items;

import javafx.scene.Node;
import org.marid.ide.common.IdeShapes;
import org.marid.spring.xml.*;

/**
 * @author Dmitry Ovchinnikov
 */
public interface TreeItemUtils {

    static String itemText(DElement<?> element) {
        if (element instanceof DRef) {
            return ((DRef) element).getBean();
        } else if (element instanceof DValue) {
            return ((DValue) element).getValue();
        } else if (element instanceof DCollection) {
            return "[" + ((DCollection) element).elements.size() + "]";
        } else if (element instanceof DMap) {
            return "{" + ((DMap) element).entries.size() + "}";
        } else if (element instanceof DProps) {
            return "{:" + ((DProps) element).entries.size() + ":}";
        }
        return null;
    }

    static Node itemGraphic(DElement<?> element) {
        if (element instanceof DRef) {
            return IdeShapes.ref(((DRef) element), 20);
        } else if (element instanceof BeanData) {
            return IdeShapes.beanNode(((BeanData) element), 20);
        }
        return null;
    }
}
