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

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.marid.dependant.beaneditor.model.BeanFactoryMethod;
import org.marid.ide.model.Annotations;
import org.marid.java.JavaFileHolder;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTableActions {

    private final JavaFileHolder updater;

    @Autowired
    public BeanTableActions(JavaFileHolder updater) {
        this.updater = updater;
    }

    @Bean
    @Qualifier("beanTable")
    public Function<MethodDeclaration, FxAction> addActionSupplier(SpecialAction addAction) {
        return m -> new FxAction("beans", "beans", "beans")
                .setSpecialAction(addAction)
                .setEventHandler(event -> {
                    final ClassOrInterfaceDeclaration type = updater.getCompilationUnit().getTypes().stream()
                            .filter(ClassOrInterfaceDeclaration.class::isInstance)
                            .map(ClassOrInterfaceDeclaration.class::cast)
                            .findFirst()
                            .orElseThrow(IllegalStateException::new);

                    type.addMethod("bean1", Modifier.PUBLIC)
                            .setBody(new BlockStmt(NodeList.nodeList(
                                    new ReturnStmt(new NullLiteralExpr())
                            )))
                            .setType(Void.class.getName())
                            .addAnnotation(Annotations.generated("org.marid"))
                            .addAnnotation(Annotations.bean());
                    updater.save();
                })
                .bindText("Add a bean")
                .setDisabled(false);
    }

    @Bean
    @Qualifier("beanTable")
    public Function<BeanFactoryMethod, Collection<FxAction>> tableActions(
            @Qualifier("beanTable") List<Function<MethodDeclaration, FxAction>> suppliers) {
        return m -> suppliers.stream()
                .map(f -> f.apply(m == null ? null : m.method.get()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
