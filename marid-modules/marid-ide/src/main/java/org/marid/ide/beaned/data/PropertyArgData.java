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
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Dmitry Ovchinnikov
 */
public class PropertyArgData implements RefData {

    private final String name;
    private final String type;
    private final StringProperty value = new SimpleStringProperty();
    private final StringProperty ref = new SimpleStringProperty();

    public PropertyArgData(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getRef() {
        return ref.get();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value.get();
    }

    @Override
    public StringProperty nameProperty() {
        return new SimpleStringProperty(name);
    }

    @Override
    public boolean isNameEditable() {
        return false;
    }

    @Override
    public boolean isValueEditable() {
        return true;
    }

    @Override
    public StringProperty valueProperty() {
        return value;
    }

    @Override
    public StringProperty refProperty() {
        return ref;
    }

    @Override
    public GlyphIcons getIcon() {
        return MaterialDesignIcon.LIGHTBULB;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
