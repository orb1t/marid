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

package org.marid.dependant.beaneditor.model;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.dependant.beaneditor.BeanEditorProperties;
import org.marid.ide.model.Annotations;
import org.marid.java.JavaFileHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class BeanModelUpdater {

    private final BeanEditorProperties properties;
    private final JavaFileHolder holder;
    private final ObservableList<BeanFactoryMethod> beans = FXCollections.observableArrayList();

    @Autowired
    public BeanModelUpdater(BeanEditorProperties properties, JavaFileHolder holder) {
        this.properties = properties;
        this.holder = holder;
        holder.compilationUnitProperty().addListener(o -> updateBeans());
    }

    @PostConstruct
    public void updateBeans() {
        final List<MethodDeclaration> methods = holder.getCompilationUnit().getTypes().stream()
                .filter(TypeDeclaration::isTopLevelType)
                .filter(t -> !t.isNestedType())
                .findFirst()
                .map(t -> t.getMethods().stream()
                        .sorted(Comparator.comparing(NodeWithSimpleName::getNameAsString))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)))
                .orElseGet(FXCollections::observableArrayList);
        beans.removeIf(bfm -> methods.stream().noneMatch(m -> m.getNameAsString().equals(bfm.name.get())));
        methods.forEach(m -> {
            final BeanFactoryMethod method = beans.stream()
                    .filter(bfm -> bfm.name.get().equals(m.getNameAsString()))
                    .findFirst()
                    .orElseGet(() -> {
                        final BeanFactoryMethod bfm = new BeanFactoryMethod(m);
                        final int index = -(Collections.binarySearch(beans, bfm) + 1);
                        beans.add(index, bfm);
                        bfm.lazy.addListener((o, oV, nV) -> initLazy(bfm, nV));
                        bfm.prototype.addListener((o, oV, nV) -> initPrototype(bfm, nV));
                        return bfm;
                    });

            method.method.set(m);

            if (!Objects.equals(method.type.get(), m.getType().asString())) {
                method.type.set(m.getType().asString());
            }

            if (method.lazy.get() != Annotations.isLazy(m)) {
                method.lazy.set(Annotations.isLazy(m));
            }

            if (method.prototype.get() != Annotations.isPrototype(m)) {
                method.prototype.set(Annotations.isPrototype(m));
            }

            final List<Parameter> parameters = m.getParameters();
            method.parameters.removeIf(bp -> parameters.stream().noneMatch(p -> p.getNameAsString().equals(bp.name.get())));
            parameters.forEach(p -> {
                final BeanParameter parameter = method.parameters.stream()
                        .filter(bp -> bp.name.get().equals(p.getNameAsString()))
                        .findFirst()
                        .orElseGet(() -> {
                            final BeanParameter bp = new BeanParameter(p);
                            final int index = -(Collections.binarySearch(method.parameters, bp) + 1);
                            method.parameters.add(index, bp);
                            return bp;
                        });

                parameter.parameter.set(p);

                if (!parameter.type.get().equals(p.getType().asString())) {
                    parameter.type.set(p.getType().asString());
                }

                if (!Objects.equals(parameter.value.get(), Annotations.value(p))) {
                    parameter.value.set(Annotations.value(p));
                }
            });

            final String namePrefix = m.getNameAsString() + ".";
            final int offset = namePrefix.length();
            final Map<String, String> properties = this.properties.properties.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(namePrefix))
                    .collect(toMap(e -> e.getKey().substring(offset), Entry::getValue, (s1, s2) -> s2, TreeMap::new));
            method.properties.removeIf(p -> !properties.containsKey(p.name.get()));
            properties.forEach((name, value) -> {
                final BeanProperty property = method.properties.stream()
                        .filter(bp -> bp.name.get().equals(name))
                        .findFirst()
                        .orElseGet(() -> {
                            final BeanProperty bp = new BeanProperty(name, value);
                            final int index = -(Collections.binarySearch(method.properties, bp) + 1);
                            method.properties.add(index, bp);
                            return bp;
                        });

                if (!Objects.equals(property.value.get(), value)) {
                    property.value.set(value);
                }
            });
        });
    }

    public ObservableList<BeanFactoryMethod> getBeans() {
        return beans;
    }

    public void initLazy(BeanFactoryMethod method, boolean lazy) {
        final MethodDeclaration declaration = method.method.get();
        final Optional<AnnotationExpr> a = declaration.getAnnotationByClass(Lazy.class);
        if (lazy) {
            if (!a.isPresent()) {
                declaration.addAnnotation(Annotations.lazy());
                holder.save();
            }
        } else {
            if (a.isPresent()) {
                declaration.remove(a.get());
                holder.save();
            }
        }
    }

    public void initPrototype(BeanFactoryMethod method, boolean prototype) {
        final MethodDeclaration declaration = method.method.get();
        final Optional<AnnotationExpr> a = declaration.getAnnotationByClass(Scope.class);
        if (prototype) {
            if (!a.isPresent()) {
                declaration.addAnnotation(Annotations.prototype());
                holder.save();
            }
        } else {
            if (a.isPresent()) {
                declaration.remove(a.get());
                holder.save();
            }
        }
    }
}
