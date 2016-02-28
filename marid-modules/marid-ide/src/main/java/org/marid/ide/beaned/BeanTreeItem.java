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

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTreeItem {

    final BeanTreeItemType itemType;
    final StringProperty nameProperty = new SimpleStringProperty();
    final StringProperty typeProperty = new SimpleStringProperty();
    final StringProperty valueProperty = new SimpleStringProperty();
    final MapProperty<String, String> mapProperty = new SimpleMapProperty<>(FXCollections.observableHashMap());

    public BeanTreeItem(BeanTreeItemType itemType, String name, String type, String value) {
        this.itemType = itemType;
        nameProperty.set(name);
        typeProperty.set(type);
        valueProperty.set(value);
    }

    public BeanTreeItem(BeanTreeItemType itemType, String name, String type) {
        this(itemType, name, type, "");
    }

    public BeanTreeItem(BeanTreeItemType itemType, String name) {
        this(itemType, name, "");
    }

    public BeanTreeItem(BeanTreeItemType itemType) {
        this(itemType, "");
    }

    public String getName() {
        return nameProperty.getName();
    }

    public String getType() {
        return typeProperty.get();
    }

    public String getValue() {
        return nameProperty.getValue();
    }

    public boolean getAsBoolean() {
        return "true".equals(valueProperty.get());
    }

    public int getAsInt() {
        return Integer.parseInt(valueProperty.get());
    }

    public Map<String, String> getProperties() {
        return mapProperty.get();
    }
}
