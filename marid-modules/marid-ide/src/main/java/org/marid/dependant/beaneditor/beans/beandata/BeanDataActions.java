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

package org.marid.dependant.beaneditor.beans.beandata;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListCell;
import org.marid.ide.project.ProjectProfileReflection;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.dialog.ListDialog;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.annotation.Q;
import org.marid.spring.xml.data.BeanData;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Executable;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.marid.misc.Reflections.parameterName;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanDataActions {

    private final BeanData beanData;
    private final ProjectProfileReflection reflection;

    public BeanDataActions(BeanData beanData, ProjectProfileReflection reflection) {
        this.beanData = beanData;
        this.reflection = reflection;
    }

    @Bean
    @Q(BeanDataActions.class)
    public FxAction refreshAction() {
        return new FxAction("refresh", "refresh", "Actions")
                .setEventHandler(event -> reflection.updateBeanData(beanData))
                .setIcon(FontIcon.M_REFRESH)
                .setText("Refresh");
    }

    @Bean
    @Q(BeanDataActions.class)
    public FxAction selectConstructorAction() {
        return new FxAction("search", "search", "Actions")
                .setEventHandler(this::onSelectConstructor)
                .setIcon(FontIcon.M_SEARCH)
                .setText("Select constructor");
    }

    public void onSelectConstructor(ActionEvent event) {
        final ObservableList<Executable> constructors = reflection.getConstructors(beanData)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        final ListDialog<Executable> dialog = new ListDialog<>("Select constructor", constructors);
        dialog.getListView().setCellFactory(param -> new ListCell<Executable>() {
            @Override
            protected void updateItem(Executable item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    final String longText = Stream.of(item.getParameters())
                            .map(p -> {
                                final String type = p.getParameterizedType() instanceof Class<?>
                                        ? ((Class) p.getParameterizedType()).getName()
                                        : p.getParameterizedType().toString();
                                return parameterName(p) + " : " + type;
                            })
                            .collect(Collectors.joining(", ", "(", ")"));
                    setGraphic(FontIcons.glyphIcon(FontIcon.M_MEMORY, 16));
                    setText(longText);
                }
            }
        });
        dialog.getDialogPane().setPrefWidth(1024);
        dialog.setResizable(true);
        final Optional<Executable> result = dialog.showAndWait();
        if (result.isPresent()) {
            reflection.updateBeanDataConstructorArgs(beanData, result.get().getParameters());
        }
    }
}
