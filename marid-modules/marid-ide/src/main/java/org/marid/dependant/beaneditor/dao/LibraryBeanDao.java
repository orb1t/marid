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

package org.marid.dependant.beaneditor.dao;

import org.marid.annotation.MetaLiteral;
import org.marid.dependant.beaneditor.model.LibraryBean;
import org.marid.ide.project.ProjectProfile;
import org.marid.runtime.annotation.MaridBean;
import org.marid.runtime.annotation.MaridBeanProducer;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.*;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;
import static org.marid.annotation.MetaLiteral.l;
import static org.marid.logging.Log.log;
import static org.marid.misc.Urls.lines;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class LibraryBeanDao {

    public static final String BEAN_CLASSES = "META-INF/marid/bean-classes.lst";

    private final ProjectProfile profile;

    @Autowired
    public LibraryBeanDao(ProjectProfile profile) {
        this.profile = profile;
    }

    public LibraryBean[] beans() {
        return lines(profile.getClassLoader(), BEAN_CLASSES, this::beans).flatMap(v -> v).toArray(LibraryBean[]::new);
    }

    public Stream<LibraryBean> beans(String type) {
        if (type.isEmpty() || type.startsWith("#")) {
            return Stream.empty();
        }
        final Class<?> c;
        try {
            c = Class.forName(type, false, profile.getClassLoader());
        } catch (ClassNotFoundException x) {
            log(WARNING, "Class {0} is not found", x, type);
            return Stream.empty();
        }
        if (!c.isAnnotationPresent(MaridBean.class)) {
            log(WARNING, "{0} is not annotated with {1}", c, MaridBean.class.getName());
            return Stream.empty();
        }
        final Stream.Builder<LibraryBean> builder = Stream.builder();
        for (final Constructor<?> constructor: c.getConstructors()) {
            if (constructor.getParameterCount() > 0 && !constructor.isAnnotationPresent(MaridBeanProducer.class)) {
                continue;
            }
            final MetaLiteral literal = l("Bean", "Other", c, "D_SERVER_NETWORK", constructor);
            final Bean bean = new Bean(literal.name, c.getName(), new BeanMethod(constructor, args(constructor)));
            builder.accept(new LibraryBean(bean, literal));
        }
        for (final Method method : c.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                if (!method.isAnnotationPresent(MaridBeanProducer.class)) {
                    continue;
                }
                final MetaLiteral literal = l("Bean", "Other", method.getName(), "D_SERVER_NETWORK", method);
                final Bean bean = new Bean(literal.name, c.getName(), new BeanMethod(method, args(method)));
                builder.accept(new LibraryBean(bean, literal));
            }
        }
        for (final Field field : c.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                if (!field.isAnnotationPresent(MaridBeanProducer.class)) {
                    continue;
                }
                final MetaLiteral literal = l("Bean", "Other", field.getName(), "D_SERVER_NETWORK", field);
                final Bean bean = new Bean(literal.name, c.getName(), new BeanMethod(field));
                builder.accept(new LibraryBean(bean, literal));
            }
        }
        return builder.build();
    }

    public static BeanMethodArg[] args(Executable executable) {
        return Stream.of(executable.getParameters())
                .map(p -> new BeanMethodArg(p.getName(), "default", null, null))
                .toArray(BeanMethodArg[]::new);
    }
}
