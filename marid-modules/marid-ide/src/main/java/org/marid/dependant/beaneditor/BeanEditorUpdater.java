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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.event.TextFileChangedEvent;
import org.marid.ide.event.TextFileMovedEvent;
import org.marid.ide.model.TextFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Service
public class BeanEditorUpdater {

    private final ObservableList<MethodDeclaration> methods = FXCollections.observableArrayList();
    private final TextFile textFile;

    @Autowired
    public BeanEditorUpdater(TextFile textFile) {
        this.textFile = textFile;
    }

    @PostConstruct
    public void update() {
        try {
            final CompilationUnit compilationUnit = JavaParser.parse(textFile.getPath(), UTF_8);
            compilationUnit.getTypes().stream()
                    .filter(ClassOrInterfaceDeclaration.class::isInstance)
                    .map(ClassOrInterfaceDeclaration.class::cast)
                    .filter(t -> !t.isInterface())
                    .findFirst()
                    .ifPresent(t -> {
                        methods.removeIf(d -> t.getMethods().stream()
                                .noneMatch(m -> m.getNameAsString().equals(d.getNameAsString()))
                        );
                        t.getMethods().forEach(m -> {
                            final int i = binarySearch(methods, m, comparing(MethodDeclaration::getNameAsString));
                            if (i >= 0) {
                                if (!m.equals(methods.get(i))) {
                                    methods.set(i, m);
                                }
                            } else {
                                methods.add(-(i + 1), m);
                            }
                        });
                    });
        } catch (Exception x) {
            log(WARNING, "Unable to compile {0}", x, textFile);
        }
    }

    public ObservableList<MethodDeclaration> getMethods() {
        return methods;
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
