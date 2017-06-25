/*
 *
 */

package org.marid.jfx.props;

/*-
 * #%L
 * marid-fx
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import org.marid.jfx.icons.FontIcons;

/**
 * @author Dmitry Ovchinnikov
 */
@SuppressWarnings("unchecked")
public interface GraphicIcons<T extends GraphicIcons<T>> {

    ObjectProperty<Node> graphicProperty();

    default T icon(String icon, int size) {
        graphicProperty().set(FontIcons.glyphIcon(icon, size));
        return (T) this;
    }

    default T icon(String icon) {
        return icon(icon, 16);
    }
}
