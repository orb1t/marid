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

package org.marid.dependant.beaneditor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.marid.Ide;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.spring.xml.BeanData;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanDataDetailsEditor extends Dialog<Boolean> {

    private final BeanData beanData;
    private final ProjectProfile profile;
    private final List<Runnable> acceptActions = new ArrayList<>();

    public BeanDataDetailsEditor(BeanData beanData, ProjectProfile profile) {
        this.beanData = beanData;
        this.profile = profile;
        initOwner(Ide.primaryStage);
        initModality(Modality.WINDOW_MODAL);
        setResizable(true);
        setTitle(s("Bean details"));
        getDialogPane().setContent(tabPane());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setPrefWidth(800);
        setResultConverter(param -> {
            if (param.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                acceptActions.forEach(Runnable::run);
                return true;
            } else {
                return null;
            }
        });
    }

    private ComboBox<String> lazyCombo() {
        final ComboBox<String> lazy = new ComboBox<>(FXCollections.observableArrayList("default", "true", "false"));
        lazy.setValue(beanData.lazyInit.get());
        lazy.setMaxWidth(Double.MAX_VALUE);
        acceptActions.add(() -> beanData.lazyInit.set(lazy.getValue()));
        return lazy;
    }

    private ComboBox<String> initMethodCombo() {
        final ComboBox<String> initMethod = methodCombo();
        initMethod.setValue(beanData.initMethod.get());
        initMethod.setMaxWidth(Double.MAX_VALUE);
        acceptActions.add(() -> beanData.initMethod.set(initMethod.getValue()));
        return initMethod;
    }

    private ComboBox<String> destroyMethodCombo() {
        final ComboBox<String> destroyMethod = methodCombo();
        destroyMethod.setValue(beanData.destroyMethod.get());
        destroyMethod.setMaxWidth(Double.MAX_VALUE);
        acceptActions.add(() -> beanData.destroyMethod.set(destroyMethod.getValue()));
        return destroyMethod;
    }

    private GenericGridPane propertiesGridPane() {
        final GenericGridPane gridPane = new GenericGridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.addControl("Lazy", this::lazyCombo);
        gridPane.addControl("Init method", this::initMethodCombo);
        gridPane.addControl("Destroy method", this::destroyMethodCombo);
        return gridPane;
    }

    private TabPane tabPane() {
        final TabPane tabPane = new TabPane(
                new Tab(s("Properties"), new MaridScrollPane(propertiesGridPane())),
                new Tab(s("Init triggers"), triggersPane(beanData.initTriggers)),
                new Tab(s("Destroy triggers"), triggersPane(beanData.destroyTriggers))
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }

    private BorderPane triggersPane(ObservableList<String> triggers) {
        final BorderPane pane = new BorderPane();
        final ListView<String> triggerList = new ListView<>(triggers);
        pane.setCenter(new MaridScrollPane(triggerList));
        return pane;
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
        return new ComboBox<>(list);
    }
}
