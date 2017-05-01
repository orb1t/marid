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

package org.marid.dependant.beaneditor.beandata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.control.MaridControls;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.spring.xml.BeanData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.marid.jfx.icons.FontIcon.D_WINDOW_RESTORE;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanDataDetails {

    private final BeanData beanData;
    private final ProjectProfile profile;
    private final ComboBox<String> lazy;
    private final ComboBox<String> initMethod;
    private final ComboBox<String> destroyMethod;
    private final String oldLazy;
    private final String oldInitMethod;
    private final String oldDestroyMethod;

    @Autowired
    public BeanDataDetails(BeanData data, ProjectProfile profile) {
        this.beanData = data;
        this.profile = profile;
        this.lazy = lazyCombo();
        this.initMethod = initMethodCombo();
        this.destroyMethod = destroyMethodCombo();
        this.oldLazy = data.lazyInit.get();
        this.oldInitMethod = data.initMethod.get();
        this.oldDestroyMethod = data.destroyMethod.get();
    }

    @Bean
    @Order(4)
    @Qualifier("beanData")
    public Tab detailsTab() {
        final BorderPane pane = new BorderPane();
        pane.setCenter(MaridControls.createMaridScrollPane(propertiesGridPane()));
        pane.setBottom(buttonPane(event -> {
            beanData.lazyInit.set(oldLazy);
            beanData.initMethod.set(oldInitMethod);
            beanData.destroyMethod.set(oldDestroyMethod);
        }));
        return new Tab(s("Details"), pane);
    }

    private ComboBox<String> lazyCombo() {
        final ComboBox<String> lazy = new ComboBox<>(FXCollections.observableArrayList("default", "true", "false"));
        lazy.valueProperty().bindBidirectional(beanData.lazyInit);
        lazy.setMaxWidth(Double.MAX_VALUE);
        return lazy;
    }

    private ComboBox<String> initMethodCombo() {
        final ComboBox<String> initMethod = methodCombo();
        initMethod.valueProperty().bindBidirectional(beanData.initMethod);
        return initMethod;
    }

    private ComboBox<String> destroyMethodCombo() {
        final ComboBox<String> destroyMethod = methodCombo();
        destroyMethod.valueProperty().bindBidirectional(beanData.destroyMethod);
        return destroyMethod;
    }

    private HBox buttonPane(EventHandler<ActionEvent> restoreAction) {
        final HBox box = new HBox();
        box.setAlignment(Pos.BASELINE_RIGHT);
        box.setPadding(new Insets(10));
        box.getChildren().add(build(new Button(s("Restore"), glyphIcon(D_WINDOW_RESTORE, 24)), b -> {
            b.setOnAction(restoreAction);
        }));
        return box;
    }

    private GenericGridPane propertiesGridPane() {
        final GenericGridPane gridPane = new GenericGridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.addControl("Lazy", () -> lazy);
        gridPane.addControl("Init method", () -> initMethod);
        gridPane.addControl("Destroy method", () -> destroyMethod);
        return gridPane;
    }

    private ComboBox<String> methodCombo() {
        final ObservableList<String> list = FXCollections.observableArrayList();
        profile.getClass(beanData).ifPresent(c -> {
            list.add("");
            Stream.of(c.getMethods())
                    .filter(method -> method.getParameterCount() == 0)
                    .filter(method -> method.getReturnType() == void.class)
                    .filter(method -> method.getDeclaringClass() != Object.class)
                    .filter(method -> !method.isAnnotationPresent(Autowired.class))
                    .filter(method -> !method.isAnnotationPresent(PostConstruct.class))
                    .filter(method -> !method.isAnnotationPresent(PreDestroy.class))
                    .filter(method -> !"close".equals(method.getName()))
                    .filter(method -> !"destroy".equals(method.getName()))
                    .map(Method::getName)
                    .forEach(list::add);
        });
        final ComboBox<String> comboBox = new ComboBox<>(list);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        return comboBox;
    }
}
