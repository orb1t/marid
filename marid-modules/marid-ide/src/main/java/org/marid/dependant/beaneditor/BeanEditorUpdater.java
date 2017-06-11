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
