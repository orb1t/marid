package org.marid.dependant.beaneditor;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import org.marid.ide.model.Annotations;
import org.marid.jfx.LocalizedStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanPropertyTable extends TableView<Pair<String, String>> {

    public BeanPropertyTable() {
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    @Autowired
    @Order(1)
    private void keyColumn() {
        final TableColumn<Pair<String, String>, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Property"));
        column.setMinWidth(150);
        column.setPrefWidth(250);
        column.setMaxWidth(500);
        column.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getKey()));
        getColumns().add(column);
    }

    @Autowired
    @Order(2)
    private void valueColumn() {
        final TableColumn<Pair<String, String>, String> column = new TableColumn<>();
        column.textProperty().bind(LocalizedStrings.ls("Value"));
        column.setMinWidth(150);
        column.setPrefWidth(300);
        column.setMaxWidth(1000);
        column.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValue()));
        getColumns().add(column);
    }

    private void init(BeanEditorProperties properties, MethodDeclaration method) {
        final Map<String, Expression> annotations = method.getAnnotationByClass(Bean.class)
                .map(Annotations::getMembers)
                .orElse(Collections.emptyMap());
        final Set<String> names = new LinkedHashSet<>();
        Stream.of("value", "names")
                .map(annotations::get)
                .filter(ArrayInitializerExpr.class::isInstance)
                .map(ArrayInitializerExpr.class::cast)
                .flatMap(a -> a.getValues().stream())
                .filter(StringLiteralExpr.class::isInstance)
                .map(StringLiteralExpr.class::cast)
                .map(StringLiteralExpr::getValue)
                .forEach(names::add);
        if (names.isEmpty()) {
            names.add(method.getNameAsString());
        }
        final List<Pair<String, String>> pairs = new ArrayList<>();
        properties.properties.forEach((key, value) -> {
            for (final String name : names) {
                final String prefix = name + ".";
                if (key.startsWith(prefix)) {
                    pairs.add(new Pair<>(key.substring(prefix.length()), value));
                    return;
                }
            }
        });
        getItems().setAll(pairs);
    }

    @Autowired
    private void init(BeanEditorProperties properties, BeanTable beanTable) {
        beanTable.getSelectionModel().selectedItemProperty().addListener((o, oV, nV) -> init(properties, nV));
        properties.properties.addListener((MapChangeListener<String, String>) change -> {
            final MethodDeclaration method = beanTable.getSelectionModel().getSelectedItem();
            if (method != null) {
                init(properties, method);
            }
        });
    }
}
