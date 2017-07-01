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
import org.marid.java.JavaFileHolder;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
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
@Import({JavaFileHolder.class})
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
