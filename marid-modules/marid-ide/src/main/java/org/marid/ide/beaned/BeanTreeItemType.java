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

package org.marid.ide.beaned;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public enum BeanTreeItemType {

    ROOT(FontAwesomeIcon.ADN, false, false),
    BEAN(FontAwesomeIcon.INSTITUTION, true, false),
    CONSTRUCTOR_ARG(FontAwesomeIcon.ANCHOR, false, true),
    PROPERTY(FontAwesomeIcon.ANGELLIST, false, true);

    private final GlyphIcons icon;
    private final boolean nameEditable;
    private final boolean valueEditable;

    BeanTreeItemType(GlyphIcons icon, boolean nameEditable, boolean valueEditable) {
        this.icon = icon;
        this.nameEditable = nameEditable;
        this.valueEditable = valueEditable;
    }

    public GlyphIcons getIcon() {
        return icon;
    }

    public boolean isNameEditable() {
        return nameEditable;
    }

    public boolean isValueEditable() {
        return valueEditable;
    }
}
