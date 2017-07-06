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

package org.marid.dependant.beaneditor.beans;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.marid.ide.model.Annotations;
import org.marid.java.JavaFileHolder;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

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
    public MethodActionSupplier addBeanActionSupplier(SpecialAction addAction) {
        return (bfm, md) -> new FxAction("beans", "beans", "Beans")
                .setSpecialAction(addAction)
                .setEventHandler(event -> {
                    updater.getType().addMethod("bean1", Modifier.PUBLIC)
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
    public MethodActionSupplier removeBeanActionSupplier(SpecialAction removeAction) {
        return (bfm, md) -> md == null ? null : new FxAction("beans", "beans", "Beans")
                .setSpecialAction(removeAction)
                .setEventHandler(event -> {
                    if (updater.getType().remove(md)) {
                        log(INFO, "Removed {0}", md.getSignature());
                        updater.save();
                    }
                })
                .bindText("Remove bean")
                .setDisabled(false);
    }
}
