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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanData implements Data {

    private final StringProperty name = new SimpleStringProperty();
    private final String type;
    private final StringProperty factoryBean = new SimpleStringProperty();
    private final StringProperty factoryMethod = new SimpleStringProperty();
    private final StringProperty initMethod = new SimpleStringProperty();
    private final StringProperty destroyMethod = new SimpleStringProperty();
    private final ObservableList<String> dependsOn = FXCollections.observableArrayList();

    public BeanData(String type, String name) {
        this.name.set(name);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name.get();
    }

    public String getFactoryBean() {
        return factoryBean.get();
    }

    public String getFactoryMethod() {
        return factoryMethod.get();
    }

    public String getInitMethod() {
        return initMethod.get();
    }

    public String getDestroyMethod() {
        return destroyMethod.get();
    }

    public ObservableList<String> getDependsOn() {
        return dependsOn;
    }

    public StringProperty factoryBeanProperty() {
        return factoryBean;
    }

    public StringProperty factoryMethodProperty() {
        return factoryMethod;
    }

    public StringProperty initMethodProperty() {
        return initMethod;
    }

    public StringProperty destroyMethodProperty() {
        return destroyMethod;
    }

    @Override
    public String getValue() {
        final Map<String, Object> map = new LinkedHashMap<>();
        if (getFactoryBean() != null) {
            map.put("factoryBean", getFactoryBean());
        }
        if (getFactoryMethod() != null) {
            map.put("factoryMethod", getFactoryMethod());
        }
        if (getInitMethod() != null) {
            map.put("initMethod", getInitMethod());
        }
        if (getDestroyMethod() != null) {
            map.put("destroyMethod", getDestroyMethod());
        }
        if (!getDependsOn().isEmpty()) {
            map.put("dependsOn", getDependsOn());
        }
        return map.toString();
    }

    @Override
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public StringProperty valueProperty() {
        return new SimpleStringProperty(getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BeanData && Objects.equals(((BeanData) obj).getName(), getName());
    }

    @Override
    public GlyphIcons getIcon() {
        return MaterialDesignIcon.PIG;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getName(), getType());
    }
}
