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

import javafx.beans.property.SimpleStringProperty;
import javafx.util.Pair;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.dependant.beaneditor.dao.ConvertersDao;
import org.marid.ide.model.BeanMethodArgData;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.types.BeanTypeInfo;
import org.marid.ide.types.BeanTypeResolver;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.reflect.TypeUtils.unrollVariables;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanMethodActions {

    @Bean
    @Qualifier("methodArg")
    public Function<BeanMethodArgData, FxAction> converterAction(ConvertersDao dao, SpecialAction miscAction) {
        return a -> a == null ? null : new FxAction("misc", "misc", "misc")
                .bindText("Set a converter")
                .setIcon("D_CLIPPY")
                .setSpecialAction(miscAction)
                .setChildren(dao.getConverters(a).entrySet().stream()
                        .map(e -> new FxAction("", "", "")
                                .bindText(new SimpleStringProperty(e.getValue().name))
                                .setIcon(e.getValue().icon)
                                .setEventHandler(event -> a.type.set(e.getKey()))
                        )
                        .collect(Collectors.toList())
                )
                .setDisabled(false);
    }

    @Bean
    @Qualifier("methodArg")
    public Function<BeanMethodArgData, FxAction> refAction(BeanTypeResolver resolver,
                                                           ProjectProfile profile,
                                                           SpecialAction addAction) {
        return a -> a == null ? null : new FxAction("misc", "misc", "misc")
                .bindText("Add a bean reference")
                .setIcon("D_LINK_VARIANT")
                .setSpecialAction(addAction)
                .setChildren(profile.getBeanFile().beans.stream()
                        .filter(b -> !a.parent.parent.getName().equals(b.getName()))
                        .map(b -> new Pair<>(b, resolver.resolve(profile.getBeanContext(), b.getName())))
                        .filter(p -> {
                            final BeanTypeInfo i = p.getValue();
                            final BeanTypeInfo c = resolver.resolve(profile.getBeanContext(), a.parent.parent.getName());

                            final Type bt = i.getType();
                            final Type at = c.getParameter(a);
                            final Type atUnrolled = unrollVariables(emptyMap(), at);

                            return TypeUtils.isAssignable(bt, atUnrolled);
                        })
                        .map(p -> new FxAction("", "", "")
                                .bindText(new SimpleStringProperty(p.getKey().getName()))
                                .setIcon("D_LINK")
                                .setEventHandler(event -> {
                                    a.type.set("ref");
                                    a.value.set(p.getKey().getName());
                                })
                        )
                        .collect(Collectors.toList())
                )
                .setDisabled(false);
    }

    @Bean
    @Qualifier("methodArg")
    public Function<BeanMethodArgData, FxAction> resetArgAction(SpecialAction removeAction) {
        return a -> a == null ? null : new FxAction("misc", "misc", "misc")
                .bindText("Clear argument")
                .setIcon("D_NEEDLE")
                .setSpecialAction(removeAction)
                .setEventHandler(event -> {
                    a.type.set("of");
                    a.value.set(null);
                })
                .setDisabled(false);
    }
}
