/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.dependant.beaneditor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.event.TextFileChangedEvent;
import org.marid.ide.event.TextFileMovedEvent;
import org.marid.ide.model.TextFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.Generated;
import javax.annotation.PostConstruct;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toSet;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Service
public class BeanEditorUpdater {

    private final ObservableList<MethodDeclaration> beans = FXCollections.observableArrayList();
    private final TextFile textFile;

    @Autowired
    public BeanEditorUpdater(TextFile textFile) {
        this.textFile = textFile;
    }

    @PostConstruct
    public void update() {
        try {
            final CompilationUnit compilationUnit = JavaParser.parse(textFile.getPath(), UTF_8);
            for (final TypeDeclaration<?> typeDeclaration : compilationUnit.getTypes()) {
                if (!typeDeclaration.isTopLevelType() || !(typeDeclaration instanceof ClassOrInterfaceDeclaration)) {
                    continue;
                }
                final ClassOrInterfaceDeclaration t = (ClassOrInterfaceDeclaration) typeDeclaration;
                final Set<MethodDeclaration> ms = t.getMethods().stream()
                        .filter(m -> m.isAnnotationPresent(Bean.class))
                        .filter(m -> m.isAnnotationPresent(Generated.class))
                        .collect(toSet());
                final Set<String> names = ms.stream().map(MethodDeclaration::getNameAsString).collect(toSet());
                beans.removeIf(d -> !names.contains(d.getNameAsString()));
                for (final MethodDeclaration m : ms) {
                    final int i = binarySearch(beans, m, comparing(MethodDeclaration::getNameAsString));
                    if (i >= 0) {
                        if (!m.equals(beans.get(i))) {
                            beans.set(i, m);
                        }
                    } else {
                        beans.add(-(i + 1), m);
                    }
                }
                break;
            }
        } catch (Exception x) {
            log(WARNING, "Unable to compile {0}", x, textFile);
        }
    }

    public ObservableList<MethodDeclaration> getBeans() {
        return beans;
    }

    @EventListener(condition = "@javaFile.path.equals(#event.source)")
    public void onMove(TextFileMovedEvent event) {
        Platform.runLater(() -> {
            textFile.setPath(event.getTarget());
            update();
        });
    }

    @EventListener(condition = "@javaFile.path.equals(#event.source)")
    public void onChange(TextFileChangedEvent event) {
        Platform.runLater(this::update);
    }
}
