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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.marid.ide.beaned.BeanTree;
import org.marid.jfx.Dialogs;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.TextFields;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.l10n.L10nSupport;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.jfx.ComboBoxes.comboBox;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataEditorFactory implements L10nSupport {

    public static Dialog<Runnable> newDialog(BeanTree node, BeanContext beanContext, Data data) {
        if (data instanceof RefData) {
            return newEditor(node, beanContext, (RefData) data);
        } else if (data instanceof BeanData) {
            return newEditor(node, beanContext, (BeanData) data);
        } else {
            return null;
        }
    }

    public static Dialog<Runnable> newEditor(BeanTree node, BeanContext beanContext, BeanData beanData) {
        final StringProperty nameProperty = new SimpleStringProperty(beanData.getName());
        final StringProperty initMethodProperty = new SimpleStringProperty(beanData.getInitMethod());
        final StringProperty destroyMethodProperty = new SimpleStringProperty(beanData.getDestroyMethod());
        return Dialogs.dialog(node, beanData.getName(), () -> () -> {
            beanData.destroyMethodProperty().set(destroyMethodProperty.get());
            beanData.initMethodProperty().set(initMethodProperty.get());
            beanData.nameProperty().set(nameProperty.get());
        }, d -> {
            final GridPane gridPane = new GenericGridPane();
            d.getDialogPane().setContent(gridPane);
            final ObservableList<String> methods = beanContext.beanInfo(beanData.getType()).getMethods().stream()
                    .filter(m -> m.getReturnType() == void.class)
                    .map(Method::getName)
                    .sorted()
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            gridPane.addRow(0, new Label("name"), TextFields.textField(nameProperty));
            gridPane.addRow(1, new Label("initMethod"), comboBox(observableArrayList(methods), initMethodProperty));
            gridPane.addRow(2, new Label("destroyMethod"), comboBox(observableArrayList(methods), destroyMethodProperty));
        });
    }

    public static Dialog<Runnable> newEditor(BeanTree node, BeanContext beanContext, RefData refData) {
        final StringProperty valueProperty = new SimpleStringProperty(refData.getValue());
        return Dialogs.dialog(node, refData.getName(), () -> () -> {
            refData.valueProperty().set(valueProperty.get());
        }, d -> {
            final TextArea value = new TextArea();
            value.textProperty().bindBidirectional(valueProperty);
            final GridPane gridPane = new GenericGridPane();
            d.getDialogPane().setContent(gridPane);
            final ScrollPane scrollPane = ScrollPanes.scrollPane(value);
            scrollPane.setPrefSize(1000, 1000);
            gridPane.add(scrollPane, 0, 1, 2, 1);
        });
    }
}
