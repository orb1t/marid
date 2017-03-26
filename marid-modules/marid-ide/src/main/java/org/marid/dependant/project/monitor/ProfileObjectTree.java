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

package org.marid.dependant.project.monitor;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.tuple.Triple;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.beans.FxObservable;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.AbstractData;
import org.marid.spring.xml.DRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.marid.jfx.LocalizedStrings.LOCALE;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ProfileObjectTree extends TableView<Triple<AbstractData<?>, FxObservable, List<?>>> {

    private final ProjectProfile profile;

    @Autowired
    public ProfileObjectTree(ProjectProfile profile) {
        this.profile = profile;
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
        setEditable(false);
    }

    @OrderedInit(1)
    public void classColumn() {
        final TableColumn<Triple<AbstractData<?>, FxObservable, List<?>>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Type"));
        col.setPrefWidth(100);
        col.setMaxWidth(200);
        col.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getLeft().getClass().getSimpleName()));
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void dataColumn() {
        final TableColumn<Triple<AbstractData<?>, FxObservable, List<?>>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Data"));
        col.setPrefWidth(300);
        col.setMaxWidth(500);
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLeft().toString()));
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void observableColumn() {
        final TableColumn<Triple<AbstractData<?>, FxObservable, List<?>>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Observable object"));
        col.setPrefWidth(300);
        col.setMaxWidth(500);
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getMiddle().toString()));
        getColumns().add(col);
    }

    @OrderedInit(4)
    public void countColumn() {
        final TableColumn<Triple<AbstractData<?>, FxObservable, List<?>>, Number> col = new TableColumn<>();
        col.textProperty().bind(ls("Count"));
        col.setPrefWidth(60);
        col.setMaxWidth(100);
        col.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getRight().size()));
        getColumns().add(col);
    }

    @OrderedInit(5)
    public void listenersColumn() {
        final TableColumn<Triple<AbstractData<?>, FxObservable, List<?>>, Label> col = new TableColumn<>();
        col.textProperty().bind(ls("Listeners"));
        col.setPrefWidth(600);
        col.setMaxWidth(1000);
        col.setCellValueFactory(p -> {
            final String text = p.getValue().getRight().stream().map(o -> o.getClass().getName()).collect(joining(","));
            final Label label = new Label(text);
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem menuItem = new MenuItem(s("Details"));
            menuItem.setOnAction(event -> {
                final ListView<String> listView = new ListView<>(p.getValue().getRight().stream()
                        .map(Object::toString)
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)));
                final Stage stage = new Stage(StageStyle.UTILITY);
                stage.setTitle(s("Details"));
                stage.setScene(new Scene(listView, 800, 800));
                stage.show();
            });
            contextMenu.getItems().add(menuItem);
            label.setContextMenu(contextMenu);
            return new SimpleObjectProperty<>(label);
        });
        getColumns().add(col);
    }

    @Scheduled(fixedDelay = 1_000L)
    public void update() {
        final List<Triple<AbstractData<?>, FxObservable, List<?>>> list = profile.getBeanFiles().stream()
                .flatMap(ProfileObjectTree::stream)
                .flatMap(d -> d.observableStream().map(o -> {
                    final List<?> listeners = o.listeners().collect(toList());
                    return Triple.<AbstractData<?>, FxObservable, List<?>>of(d, o, listeners);
                }))
                .collect(toList());

        list.add(Triple.of(new DRef("locale"), LOCALE, LOCALE.listeners().collect(toList())));
        Platform.runLater(() -> getItems().setAll(list));
    }

    private static Stream<? extends AbstractData<?>> stream(AbstractData<?> data) {
        return Stream.concat(Stream.of(data), data.stream().flatMap(ProfileObjectTree::stream));
    }
}
