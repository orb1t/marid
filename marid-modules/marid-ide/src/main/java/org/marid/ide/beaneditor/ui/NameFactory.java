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

package org.marid.ide.beaneditor.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.ConstructorArg;
import org.marid.ide.beaneditor.data.Property;
import org.marid.ide.project.ProjectProfile;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class NameFactory implements Callback<TreeTableColumn.CellDataFeatures<Object, String>, ObservableValue<String>> {

    @Override
    public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object, String> param) {
        final TreeItem<Object> item = param.getValue();
        if (item.getValue() instanceof ProjectProfile) {
            return new SimpleStringProperty(((ProjectProfile) item.getValue()).getName());
        } else if (item.getValue() instanceof String) {
            return new SimpleStringProperty(item.getValue().toString());
        } else if (item.getValue() instanceof Path) {
            return new SimpleStringProperty(((Path) item.getValue()).getFileName().toString());
        } else if (item.getValue() instanceof BeanData) {
            return ((BeanData) item.getValue()).name;
        } else if (item.getValue() instanceof ConstructorArg) {
            return ((ConstructorArg) item.getValue()).name;
        } else if (item.getValue() instanceof Property) {
            return ((Property) item.getValue()).name;
        } else if (item.getValue() instanceof StringProperty) {
            return new SimpleStringProperty(((StringProperty) item.getValue()).getName());
        } else {
            return new SimpleStringProperty("");
        }
    }
}
