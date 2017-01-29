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
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.commons.lang3.tuple.Triple;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.AbstractData;
import org.marid.spring.xml.DRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.marid.ide.project.ProfileReflections.listeners;
import static org.marid.ide.project.ProfileReflections.observableStream;
import static org.marid.jfx.LocalizedStrings.LOCALE;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ProfileObjectTree extends TableView<Triple<? extends AbstractData<?>, Observable, List<Object>>> {

    private final ProjectProfile profile;

    @Autowired
    public ProfileObjectTree(ProjectProfile profile) {
        this.profile = profile;
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
        setEditable(false);
    }

    @OrderedInit(1)
    public void classColumn() {
        final TableColumn<Triple<? extends AbstractData<?>, Observable, List<Object>>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Type"));
        col.setPrefWidth(100);
        col.setMaxWidth(200);
        col.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getLeft().getClass().getSimpleName()));
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void dataColumn() {
        final TableColumn<Triple<? extends AbstractData<?>, Observable, List<Object>>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Data"));
        col.setPrefWidth(300);
        col.setMaxWidth(500);
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLeft().toString()));
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void observableColumn() {
        final TableColumn<Triple<? extends AbstractData<?>, Observable, List<Object>>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Observable object"));
        col.setPrefWidth(300);
        col.setMaxWidth(500);
        col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getMiddle().toString()));
        getColumns().add(col);
    }

    @OrderedInit(4)
    public void countColumn() {
        final TableColumn<Triple<? extends AbstractData<?>, Observable, List<Object>>, Number> col = new TableColumn<>();
        col.textProperty().bind(ls("Count"));
        col.setPrefWidth(60);
        col.setMaxWidth(100);
        col.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getRight().size()));
        getColumns().add(col);
    }

    @OrderedInit(5)
    public void listenersColumn() {
        final TableColumn<Triple<? extends AbstractData<?>, Observable, List<Object>>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Listeners"));
        col.setPrefWidth(600);
        col.setMaxWidth(1000);
        col.setCellValueFactory(p -> {
            final String text = p.getValue().getRight().stream().map(o -> o.getClass().getName()).collect(joining(","));
            return new SimpleStringProperty(text);
        });
        getColumns().add(col);
    }

    @Scheduled(fixedDelay = 1_000L)
    public void update() {
        final List<Triple<? extends AbstractData<?>, Observable, List<Object>>> list = observableStream(profile)
                .sorted(Comparator.comparing(k -> k.getKey().getClass().getName()))
                .flatMap(p -> Stream.of(p.getValue()).map(v -> Triple.of(p.getKey(), v, listeners(v))))
                .collect(Collectors.toList());
        list.add(Triple.of(new DRef("locale"), LOCALE, listeners(LOCALE)));
        Platform.runLater(() -> getItems().setAll(list));
    }
}
