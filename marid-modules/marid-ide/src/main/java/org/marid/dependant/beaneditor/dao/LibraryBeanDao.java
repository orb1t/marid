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

import com.google.common.reflect.TypeToken;
import org.marid.annotation.MetaLiteral;
import org.marid.dependant.beaneditor.model.LibraryBean;
import org.marid.dependant.beaneditor.model.LibraryMethod;
import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.types.BeanContext;
import org.marid.ide.types.BeanTypeInfo;
import org.marid.ide.types.BeanTypeResolver;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static com.github.javaparser.utils.Utils.decapitalize;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Stream.of;
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
    private final BeanTypeResolver resolver;

    @Autowired
    public LibraryBeanDao(ProjectProfile profile, BeanTypeResolver resolver) {
        this.profile = profile;
        this.resolver = resolver;
    }

    public Stream<LibraryBean> beans() {
        return lines(profile.getClassLoader(), BEAN_CLASSES, this::beans).flatMap(v -> v);
    }

    public Stream<LibraryBean> beans(String type) {
        if (type.isEmpty() || type.startsWith("#")) {
            return Stream.empty();
        }
        final Class<?> c = type(type);
        return of(
                of(c.getConstructors())
                        .map(e -> {
                            final String name = decapitalize(c.getSimpleName());
                            final MetaLiteral literal = l("Bean", "Other", c, "D_SERVER_NETWORK", e);
                            final Bean bean = new Bean(name, c.getName(), new BeanMethod(e, args(e)));
                            return new LibraryBean(bean, literal);
                        }),
                of(c.getMethods())
                        .filter(m -> Modifier.isStatic(m.getModifiers()))
                        .filter(m -> !m.getReturnType().isPrimitive())
                        .map(e -> {
                            final MetaLiteral literal = l("Bean", "Other", e.getName(), "D_SERVER_NETWORK", e);
                            final Bean bean = new Bean(e.getName(), c.getName(), new BeanMethod(e, args(e)));
                            return new LibraryBean(bean, literal);
                        }),
                of(c.getFields())
                        .filter(f -> Modifier.isStatic(f.getModifiers()))
                        .filter(f -> !f.getType().isPrimitive())
                        .map(e -> {
                            final MetaLiteral literal = l("Bean", "Other", e.getName(), "D_SERVER_NETWORK", e);
                            final Bean bean = new Bean(e.getName(), c.getName(), new BeanMethod(e));
                            return new LibraryBean(bean, literal);
                        })
        ).flatMap(v -> v);
    }

    public Stream<LibraryBean> beans(BeanData beanData) {
        final BeanContext cache = profile.getBeanContext();
        final BeanTypeInfo typeInfo = resolver.resolve(cache, beanData.getName());
        final Class<?> c = TypeToken.of(typeInfo.getType()).getRawType();
        return of(
                of(c.getMethods())
                        .filter(e -> !Modifier.isStatic(e.getModifiers()))
                        .filter(e -> !e.getReturnType().isPrimitive())
                        .map(e -> {
                            final MetaLiteral literal = l("Bean", "Other", e.getName(), "D_SERVER_NETWORK", e);
                            final Bean bean = new Bean(e.getName(), beanData.getName(), new BeanMethod(e, args(e)));
                            return new LibraryBean(bean, literal);
                        }),
                of(c.getFields())
                        .filter(f -> !Modifier.isStatic(f.getModifiers()))
                        .filter(f -> !f.getType().isPrimitive())
                        .map(e -> {
                            final MetaLiteral literal = l("Bean", "Other", e.getName(), "D_SERVER_NETWORK", e);
                            final Bean bean = new Bean(e.getName(), beanData.getName(), new BeanMethod(e));
                            return new LibraryBean(bean, literal);
                        })
        ).flatMap(v -> v);
    }

    public Stream<LibraryMethod> initializers(BeanData beanData) {
        final BeanContext cache = profile.getBeanContext();
        final BeanTypeInfo typeInfo = resolver.resolve(cache, beanData.getName());
        final Class<?> c = TypeToken.of(typeInfo.getType()).getRawType();
        return of(
                of(c.getMethods())
                        .filter(e -> !Modifier.isStatic(e.getModifiers()))
                        .filter(e -> e.getDeclaringClass() != Object.class)
                        .map(e -> {
                            final MetaLiteral literal = l("Bean", "Other", e.getName(), "D_SERVER_NETWORK", e);
                            final BeanMethod i = new BeanMethod(e, args(e));
                            return new LibraryMethod(i, literal);
                        }),
                of(c.getFields())
                        .filter(f -> !Modifier.isStatic(f.getModifiers()))
                        .map(e -> {
                            final MetaLiteral literal = l("Bean", "Other", e.getName(), "D_SERVER_NETWORK", e);
                            final BeanMethod i = new BeanMethod(e);
                            return new LibraryMethod(i, literal);
                        })
        ).flatMap(v -> v);
    }

    private Class<?> type(String type) {
        try {
            return Class.forName(type, false, profile.getClassLoader());
        } catch (ClassNotFoundException x) {
            log(WARNING, "Class {0} is not found", x, type);
        }
        return Void.class;
    }

    private static BeanMethodArg[] args(Executable executable) {
        return of(executable.getParameters())
                .map(p -> new BeanMethodArg(p.getName(), "of", null, null))
                .toArray(BeanMethodArg[]::new);
    }
}
