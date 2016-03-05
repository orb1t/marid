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

package org.marid.ide.beaned.data;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Dmitry Ovchinnikov
 */
public class RootData implements Data {

    public static final RootData ROOT_DATA = new RootData();

    private RootData() {
    }

    @Override
    public String getType() {
        return Void.class.getName();
    }

    @Override
    public String getName() {
        return "root";
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public StringProperty nameProperty() {
        return new SimpleStringProperty("");
    }

    @Override
    public StringProperty valueProperty() {
        return new SimpleStringProperty("");
    }

    @Override
    public GlyphIcons getIcon() {
        return OctIcon.BROWSER;
    }

    @Override
    public String toString() {
        return getName();
    }
}
