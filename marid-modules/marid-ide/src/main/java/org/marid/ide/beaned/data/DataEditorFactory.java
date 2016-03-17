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
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.marid.beans.meta.BeanIntrospector;
import org.marid.ide.beaned.BeanTree;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.dialog.MaridDialog;
import org.marid.jfx.panes.GenericGridPane;

import java.lang.reflect.Method;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.ServiceLoader.load;
import static javafx.collections.FXCollections.observableArrayList;
import static org.marid.jfx.ComboBoxes.comboBox;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataEditorFactory {

    public static Dialog<Runnable> newDialog(BeanTree node, BeanContext beanContext, Data data) {
        if (data instanceof RefData) {
            return newEditor(node, beanContext, (RefData) data);
        } else if (data instanceof BeanData) {
            return newEditor(node, beanContext, (BeanData) data);
        }
        return null;
    }

    public static Dialog<Runnable> newEditor(BeanTree tree, BeanContext beanContext, BeanData beanData) {
        final StringProperty nameProperty = new SimpleStringProperty(beanData.getName());
        final StringProperty initMethodProperty = new SimpleStringProperty(beanData.getInitMethod());
        final StringProperty destroyMethodProperty = new SimpleStringProperty(beanData.getDestroyMethod());
        final ObservableList<String> methods = beanContext.beanInfo(beanData.getType()).getMethods().stream()
                .filter(m -> m.getReturnType() == void.class)
                .map(Method::getName)
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        return new MaridDialog<Runnable>(tree)
                .title(beanData.getName())
                .with(GenericGridPane::new, (d, p) -> {
                    p.addTextField("Name", nameProperty);
                    p.addControl("Initialize method", () -> comboBox(observableArrayList(methods), initMethodProperty));
                    p.addControl("Destroy method", () -> comboBox(observableArrayList(methods), destroyMethodProperty));
                })
                .result(() -> () -> {
                    beanData.destroyMethodProperty().set(destroyMethodProperty.get());
                    beanData.initMethodProperty().set(initMethodProperty.get());
                    beanData.nameProperty().set(nameProperty.get());
                });
    }

    public static Dialog<Runnable> newEditor(BeanTree tree, BeanContext beanContext, RefData refData) {
        final StringProperty valueProperty = new SimpleStringProperty(refData.getValue());
        final StringProperty refProperty = new SimpleStringProperty(refData.getRef());
        final ServiceLoader<BeanIntrospector> introspectors = load(BeanIntrospector.class, beanContext.classLoader);
        final ObservableList<String> refs = FXCollections.observableArrayList();
        final BeanInfo curBean = beanContext.beanInfo(refData.getType());
        introspectors.forEach(introspector ->
                Stream.of(introspector.getBeans(beanContext.classLoader)).forEach(beanInfo -> {
                    final BeanInfo refBean = beanContext.beanInfo(beanInfo.getType());
                    if (curBean.getType().isAssignableFrom(refBean.getType())) {
                        refs.add(beanInfo.getName());
                    }
                }));
        valueProperty.addListener((observable, oldValue, newValue) -> {
            refProperty.set("");
        });
        refProperty.addListener((observable, oldValue, newValue) -> {
            valueProperty.set("");
        });
        return new MaridDialog<Runnable>(tree)
                .title(refData.getName())
                .with(GenericGridPane::new, (d, p) -> {
                    d.setResizable(true);
                    p.addControl("Reference", () -> comboBox(refs, refProperty));
                    final ScrollPane valueScrollPane = p.addControl("Value", () -> {
                        final TextArea valueArea = new TextArea();
                        valueArea.textProperty().bindBidirectional(valueProperty);
                        return ScrollPanes.scrollPane(valueArea);
                    });
                    GridPane.setVgrow(valueScrollPane, Priority.ALWAYS);
                })
                .result(() -> () -> {
                    refData.valueProperty().set(valueProperty.get());
                    refData.refProperty().set(refProperty.get());
                });
    }
}
