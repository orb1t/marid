/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableStringValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.event.TextFileMovedEvent;
import org.marid.ide.event.TextFileRemovedEvent;
import org.marid.ide.model.TextFile;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.marid.ide.common.IdeShapes.circle;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan
public class BeanEditorConfiguration extends DependantConfiguration<BeanEditorParam> {

    @Bean
    public TextFile javaFile() {
        return param.javaFile;
    }

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }

    @Bean
    public ObservableStringValue beanEditorTabText(ProjectProfile profile, TextFile javaFile) {
        return Bindings.createStringBinding(() -> {
            final Path baseDir = profile.getJavaBaseDir(javaFile.getPath());
            if (baseDir == null) {
                return null;
            }
            final Path relativePath = baseDir.relativize(javaFile.getPath());
            return StreamSupport.stream(relativePath.spliterator(), false)
                    .map(Path::toString)
                    .collect(Collectors.joining(".", "[" + profile + "] ", ""));
        });
    }

    @Bean
    public Supplier<Node> beanEditorGraphic(ProjectProfile profile, TextFile javaFile) {
        return () -> new HBox(3, circle(profile.hashCode(), 16), IdeShapes.javaFile(javaFile.hashCode(), 16));
    }

    @Bean
    public SplitPane beanSplitPane(BeanTable list, SplitPane argsSplitPane) {
        final SplitPane pane = new SplitPane(list, argsSplitPane);
        pane.setOrientation(Orientation.HORIZONTAL);
        pane.setDividerPositions(0.3);
        return pane;
    }

    @Bean
    public SplitPane argsSplitPane(BeanParameterTable parameterTable, BeanPropertyTable propertyTable) {
        final SplitPane pane = new SplitPane(parameterTable, propertyTable);
        pane.setOrientation(Orientation.VERTICAL);
        pane.setDividerPositions(0.5);
        return pane;
    }

    @Bean
    public ApplicationListener<TextFileRemovedEvent> removeListener(TextFile javaFile, GenericApplicationContext ctx) {
        return event -> {
            if (javaFile.getPath().equals(event.getSource())) {
                Platform.runLater(ctx::close);
            }
        };
    }

    @Bean
    private ApplicationListener<TextFileMovedEvent> renameListener(TextFile file) {
        return event -> {
            if (file.getPath().equals(event.getSource())) {
                Platform.runLater(() -> file.path.set(event.getTarget()));
            }
        };
    }
}
