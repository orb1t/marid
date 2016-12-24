/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.IdeDependants;
import org.marid.beans.BeanIntrospector;
import org.marid.beans.ClassInfo;
import org.marid.beans.MethodInfo;
import org.marid.beans.TypeInfo;
import org.marid.ide.project.ProfileInfo;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcon;
import org.marid.spring.beandata.BeanEditorContext;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DValue;
import org.marid.spring.xml.ref.DRef;
import org.marid.util.Reflections;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.of;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanListActions {

    final ProjectProfile profile;

    private final IdeDependants dependants;
    private final BeanListTable table;

    @Autowired
    public BeanListActions(ProjectProfile profile, IdeDependants dependants, BeanListTable table) {
        this.profile = profile;
        this.dependants = dependants;
        this.table = table;
    }

    public BeanData beanData(String name, BeanDefinition def) {
        final BeanData beanData = new BeanData();
        beanData.name.set(profile.generateBeanName(name));
        beanData.factoryBean.set(def.getFactoryBeanName());
        beanData.factoryMethod.set(def.getFactoryMethodName());
        beanData.type.set(def.getBeanClassName());
        beanData.lazyInit.set(Boolean.toString(def.isLazyInit()));

        if (def instanceof AbstractBeanDefinition) {
            final AbstractBeanDefinition definition = (AbstractBeanDefinition) def;
            beanData.initMethod.set(definition.getInitMethodName());
            beanData.destroyMethod.set(definition.getDestroyMethodName());
        }

        if (def.getConstructorArgumentValues() != null) {
            for (final ValueHolder holder : def.getConstructorArgumentValues().getGenericArgumentValues()) {
                final BeanProp beanArg = new BeanProp();
                beanArg.name.set(holder.getName());
                beanArg.type.set(holder.getType());
                if (holder.getValue() instanceof TypedStringValue) {
                    beanArg.data.set(new DValue(((TypedStringValue) holder.getValue()).getValue()));
                }
                beanData.beanArgs.add(beanArg);
            }
        }

        if (def.getPropertyValues() != null) {
            for (final PropertyValue propertyValue : def.getPropertyValues().getPropertyValueList()) {
                final BeanProp property = new BeanProp();
                property.name.set(propertyValue.getName());
                if (propertyValue.getValue() instanceof TypedStringValue) {
                    property.data.set(new DValue(((TypedStringValue) propertyValue.getValue()).getValue()));
                }
                beanData.properties.add(property);
            }
        }

        profile.updateBeanData(beanData);
        return beanData;
    }

    public BeanData insertItem(String name, BeanDefinition def, BeanMetaInfoProvider.BeansMetaInfo metaInfo) {
        final BeanData beanData = beanData(name, def);
        profile.updateBeanData(beanData);
        if (def.getConstructorArgumentValues() != null) {
            for (final ValueHolder valueHolder : def.getConstructorArgumentValues().getGenericArgumentValues()) {
                if (valueHolder.getValue() instanceof RuntimeBeanReference) {
                    final RuntimeBeanReference reference = (RuntimeBeanReference) valueHolder.getValue();
                    final BeanDefinition beanDefinition = metaInfo.getBeanDefinition(reference.getBeanName());
                    insertItem(reference.getBeanName(), beanDefinition, metaInfo);
                    beanData.beanArgs.filtered(a -> a.name.isEqualTo(valueHolder.getName()).get()).forEach(a -> {
                        final DRef ref = new DRef();
                        ref.setBean(reference.getBeanName());
                        a.data.setValue(ref);
                    });
                }
            }
        }
        if (def.getPropertyValues() != null && def.getPropertyValues().getPropertyValues() != null) {
            for (final PropertyValue propertyValue : def.getPropertyValues().getPropertyValues()) {
                if (propertyValue.getValue() instanceof RuntimeBeanReference) {
                    final RuntimeBeanReference reference = (RuntimeBeanReference) propertyValue.getValue();
                    final BeanDefinition beanDefinition = metaInfo.getBeanDefinition(reference.getBeanName());
                    insertItem(reference.getBeanName(), beanDefinition, metaInfo);
                    beanData.properties.filtered(a -> a.name.isEqualTo(propertyValue.getName()).get()).forEach(p -> {
                        final DRef ref = new DRef();
                        ref.setBean(reference.getBeanName());
                        p.data.setValue(ref);
                    });
                }
            }
        }
        insertItem(beanData);
        if (def.getFactoryBeanName() != null) {
            name = def.getFactoryBeanName();
            def = metaInfo.getBeanDefinition(name);
            if (def != null && !profile.containsBean(name)) {
                insertItem(name, def, metaInfo);
            }
        }
        return beanData;
    }

    public void insertItem(BeanData beanData) {
        table.getItems().add(beanData);
    }

    public List<MenuItem> factoryItems(Class<?> type, BeanData beanData) {
        final List<MenuItem> items = new ArrayList<>();
        final Set<Method> getters = of(type.getMethods())
                .filter(m -> m.getReturnType() != void.class)
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> m.getParameterCount() == 0)
                .sorted(Comparator.comparing(Method::getName))
                .filter(m -> m.getName().startsWith("get") || m.getName().startsWith("is"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<Method> producers = of(type.getMethods())
                .filter(m -> m.getReturnType() != void.class)
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> !getters.contains(m))
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<Method> parameterizedProducers = of(type.getMethods())
                .filter(m -> m.getReturnType() != void.class)
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> m.getParameterCount() > 0)
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        final Function<Method, MenuItem> menuItemFunction = method -> {
            final String name = of(method.getParameters())
                    .map(Parameter::getParameterizedType)
                    .map(t -> {
                        if (t instanceof Class<?>) {
                            final Class<?> klass = (Class<?>) t;
                            if (klass.getName().startsWith("java.lang.")) {
                                return klass.getSimpleName();
                            }
                            return klass.getName();
                        } else {
                            return t.toString();
                        }
                    })
                    .collect(joining(",", method.getName() + "(", ") : " + method.getGenericReturnType()));
            final MenuItem menuItem = new MenuItem(name, glyphIcon(FontIcon.M_MEMORY, 16));
            menuItem.setOnAction(ev -> {
                final BeanData newBeanData = new BeanData();
                newBeanData.name.set(profile.generateBeanName(method.getName()));
                newBeanData.factoryBean.set(beanData.name.get());
                newBeanData.factoryMethod.set(method.getName());
                for (final Parameter parameter : method.getParameters()) {
                    final BeanProp arg = new BeanProp();
                    arg.name.set(Reflections.parameterName(parameter));
                    arg.type.set(parameter.getType().getName());
                    newBeanData.beanArgs.add(arg);
                }
                table.getItems().add(newBeanData);
                profile.updateBeanData(newBeanData);
            });
            return menuItem;
        };
        if (!getters.isEmpty()) {
            final Menu menu = new Menu(s("Getters"));
            getters.forEach(method -> menu.getItems().add(menuItemFunction.apply(method)));
            items.add(menu);
        }
        if (!producers.isEmpty()) {
            final Menu menu = new Menu(s("Producers"));
            producers.forEach(method -> menu.getItems().add(menuItemFunction.apply(method)));
            items.add(menu);
        }
        if (!parameterizedProducers.isEmpty()) {
            final Menu menu = new Menu(s("Parameterized producers"));
            parameterizedProducers.forEach(method -> menu.getItems().add(menuItemFunction.apply(method)));
            items.add(menu);
        }
        return items;
    }

    public List<MenuItem> editors(ResolvableType type, BeanData beanData) {
        final ClassInfo classInfo = BeanIntrospector.classInfo(profile.getClassLoader(), type);
        final Class<?>[] beanArgs = profile.getConstructor(beanData)
                .map(Executable::getParameterTypes)
                .orElse(new Class<?>[0]);
        final Set<TypeInfo> editors = new HashSet<>();
        if (classInfo.editor != null) {
            editors.add(classInfo);
        }
        for (final MethodInfo constructor : classInfo.constructorInfos) {
            if (constructor.editor == null) {
                continue;
            }
            final Class<?>[] args = of(constructor.parameters).map(c -> c.type.getRawClass()).toArray(Class[]::new);
            if (Arrays.equals(args, beanArgs)) {
                editors.add(constructor);
            }
        }
        if (editors.isEmpty()) {
            return Collections.emptyList();
        } else {
            final BeanEditorContext beanEditorContext = new BeanEditorContextImpl(type, beanData);
            return editors.stream()
                    .map(d -> {
                        final Class<?> t = Objects.requireNonNull(d.editor);
                        final Configuration c = t.getAnnotation(Configuration.class);
                        final String title = c.value().isEmpty() ? t.getSimpleName() : c.value();
                        final MenuItem menuItem = new MenuItem();
                        menuItem.textProperty().bind(ls(title));
                        menuItem.setOnAction(event -> {
                            profile.updateBeanData(beanData);
                            dependants.start(t, title, ctx -> {
                                ctx.setClassLoader(profile.getClassLoader());
                                ctx.getBeanFactory().registerSingleton("context", beanEditorContext);
                            });
                        });
                        return menuItem;
                    })
                    .reduce(new ArrayList<>(singleton(new SeparatorMenuItem())), (a, e) -> {
                        a.add(e);
                        return a;
                    }, (a1, a2) -> a2);
        }
    }

    private class BeanEditorContextImpl implements BeanEditorContext {

        private final ResolvableType type;
        private final BeanData beanData;

        private BeanEditorContextImpl(ResolvableType type, BeanData beanData) {
            this.type = type;
            this.beanData = beanData;
        }

        @Override
        public BeanData getBeanData() {
            return beanData;
        }

        @Override
        public ResolvableType getType() {
            return type;
        }

        @Override
        public ProfileInfo getProfileInfo() {
            return profile;
        }

        @Override
        public Stage getPrimaryStage() {
            return Ide.primaryStage;
        }
    }
}
