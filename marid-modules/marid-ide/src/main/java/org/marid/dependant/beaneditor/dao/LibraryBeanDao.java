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
import org.marid.misc.Urls;
import org.marid.runtime.annotation.MaridBean;
import org.marid.runtime.annotation.MaridBeanProducer;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

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
        return Urls.lines(profile.getClassLoader(), BEAN_CLASSES, line -> {
            if (line.isEmpty() || line.startsWith("#")) {
                return Stream.<LibraryBean>empty();
            }
            final Class<?> c;
            try {
                c = Class.forName(line, false, profile.getClassLoader());
            } catch (ClassNotFoundException x) {
                log(WARNING, "Class {0} is not found", x, line);
                return Stream.<LibraryBean>empty();
            }
            final MaridBean maridBean = c.getAnnotation(MaridBean.class);
            if (maridBean == null) {
                log(WARNING, "{0} is not annotated with {1}", c, MaridBean.class.getName());
                return Stream.<LibraryBean>empty();
            }
            final Stream.Builder<LibraryBean> builder = Stream.builder();
            final BiConsumer<Executable, BeanMethod> consumer = (executable, method) -> {
                final MaridBeanProducer maridProducer = executable.getAnnotation(MaridBeanProducer.class);
                if (maridProducer == null) {
                    return;
                }
                final MetaLiteral literal = new MetaLiteral(
                        c.getSimpleName(),
                        "D_SERVER_NETWORK",
                        c.getName(),
                        maridBean, maridProducer
                );
                final Bean bean = new Bean(literal.name, c.getName(), method);
                builder.accept(new LibraryBean(bean, literal));
            };
            for (final Constructor<?> constructor: c.getConstructors()) {
                consumer.accept(constructor, new BeanMethod(constructor, args(constructor)));
            }
            for (final Method method : c.getMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    consumer.accept(method, new BeanMethod(method, args(method)));
                }
            }
            return builder.build();
        }).flatMap(v -> v).toArray(LibraryBean[]::new);
    }

    public static BeanMethodArg[] args(Executable executable) {
        return Stream.of(executable.getParameters())
                .map(p -> new BeanMethodArg(p.getName(), "default", null, null))
                .toArray(BeanMethodArg[]::new);
    }
}
