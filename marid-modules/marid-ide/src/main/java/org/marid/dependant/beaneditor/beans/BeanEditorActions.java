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

package org.marid.dependant.beaneditor.beans;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.props.BeanDataEditorConfiguration;
import org.marid.ide.project.ProjectCacheManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.misc.Reflections;
import org.marid.spring.beandata.BeanEditor;
import org.marid.spring.postprocessors.WindowAndDialogPostProcessor;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.ConstructorArg;
import org.marid.spring.xml.data.Property;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLClassLoader;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorActions {

    private final AnnotationConfigApplicationContext context;
    private final BeanEditorTable table;
    private final ProjectCacheManager cacheManager;
    private final ObjectProvider<Dialog<List<Entry<String, BeanDefinition>>>> beanBrowser;
    private final IdeDependants dependants;
    private final ProjectProfile profile;

    public final BooleanBinding itemActionDisabled;
    public final BooleanBinding clearDisabled;

    @Autowired
    public BeanEditorActions(AnnotationConfigApplicationContext context,
                             BeanEditorTable table,
                             ProjectCacheManager cacheManager,
                             ObjectProvider<Dialog<List<Entry<String, BeanDefinition>>>> beanBrowser,
                             IdeDependants dependants,
                             ProjectProfile profile) {
        this.context = context;
        this.table = table;
        this.cacheManager = cacheManager;
        this.beanBrowser = beanBrowser;
        this.dependants = dependants;
        this.profile = profile;

        this.itemActionDisabled = table.getSelectionModel().selectedIndexProperty().lessThan(0);
        this.clearDisabled = Bindings.isEmpty(table.getItems());
    }

    public void onEdit(ActionEvent event) {
        dependants.startDependant(BeanDataEditorConfiguration.class);
    }

    public void onDelete(ActionEvent event) {
        table.getItems().remove(table.getSelectionModel().getSelectedIndex());
    }

    public void onClear(ActionEvent event) {
        table.getItems().clear();
    }

    public void onBrowse(ActionEvent event) {
        final Optional<List<Entry<String, BeanDefinition>>> entry = beanBrowser.getObject().showAndWait();
        if (entry.isPresent()) {
            entry.get().forEach(this::insertItem);
        }
    }

    public void onAddNew(ActionEvent event) {
        final BeanData beanData = new BeanData();
        final String name = cacheManager.generateBeanName(profile, "newBean");
        beanData.name.set(name);
        beanData.type.set(Object.class.getName());
        table.getItems().add(beanData);
    }

    private void insertItem(Entry<String, BeanDefinition> entry) {
        final BeanDefinition def = entry.getValue();
        final BeanData beanData = new BeanData();
        beanData.name.set(cacheManager.generateBeanName(profile, entry.getKey()));
        beanData.factoryBean.set(def.getFactoryBeanName());
        beanData.factoryMethod.set(def.getFactoryMethodName());
        beanData.type.set(def.getBeanClassName());
        beanData.lazyInit.set(def.isLazyInit() ? "true" : null);

        if (entry.getValue() instanceof AbstractBeanDefinition) {
            final AbstractBeanDefinition definition = (AbstractBeanDefinition) entry.getValue();
            beanData.initMethod.set(definition.getInitMethodName());
            beanData.destroyMethod.set(definition.getDestroyMethodName());
        }

        if (def.getConstructorArgumentValues() != null) {
            for (final ConstructorArgumentValues.ValueHolder holder : def.getConstructorArgumentValues().getGenericArgumentValues()) {
                final ConstructorArg constructorArg = new ConstructorArg();
                constructorArg.name.set(holder.getName());
                constructorArg.type.set(holder.getType());
                if (holder.getValue() instanceof TypedStringValue) {
                    final TypedStringValue typedStringValue = (TypedStringValue) holder.getValue();
                    constructorArg.value.set(typedStringValue.getValue());
                }
                beanData.constructorArgs.add(constructorArg);
            }
        }

        if (def.getPropertyValues() != null) {
            for (final PropertyValue propertyValue : def.getPropertyValues().getPropertyValueList()) {
                final Property property = new Property();
                property.name.set(propertyValue.getName());
                if (propertyValue.getValue() instanceof TypedStringValue) {
                    final TypedStringValue typedStringValue = (TypedStringValue) propertyValue.getValue();
                    property.value.set(typedStringValue.getValue());
                }
                beanData.properties.add(property);
            }
        }

        beanData.updateBeanData(profile);

        table.getItems().add(beanData);
    }

    public void onShowPopup(ActionEvent event) {
        final Node node = (Node) event.getSource();
        final Side side = node instanceof Button ? Side.BOTTOM : Side.RIGHT;
        contextMenu(table.getSelectionModel().getSelectedItem()).show(node, side, 5, 5);
    }

    private List<MenuItem> factoryItems(Class<?> type, BeanData beanData) {
        final List<MenuItem> items = new ArrayList<>();
        final Set<Method> getters = Stream.of(type.getMethods())
                .filter(m -> m.getReturnType() != void.class)
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> m.getParameterCount() == 0)
                .sorted(Comparator.comparing(Method::getName))
                .filter(m -> m.getName().startsWith("get") || m.getName().startsWith("is"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<Method> producers = Stream.of(type.getMethods())
                .filter(m -> m.getReturnType() != void.class)
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> !getters.contains(m))
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<Method> parameterizedProducers = Stream.of(type.getMethods())
                .filter(m -> m.getReturnType() != void.class)
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> m.getParameterCount() > 0)
                .sorted(Comparator.comparing(Method::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        final Function<Method, MenuItem> menuItemFunction = method -> {
            final String name = Stream.of(method.getParameters())
                    .map(p -> p.getParameterizedType().toString())
                    .collect(joining(",", method.getName() + "(", ") : " + method.getGenericReturnType()));
            final MenuItem menuItem = new MenuItem(name, FontIcons.glyphIcon(FontIcon.M_MEMORY, 16));
            menuItem.setOnAction(ev -> {
                final BeanData newBeanData = new BeanData();
                newBeanData.name.set(cacheManager.generateBeanName(profile, method.getName()));
                newBeanData.factoryBean.set(beanData.name.get());
                newBeanData.factoryMethod.set(method.getName());
                for (final Parameter parameter : method.getParameters()) {
                    final ConstructorArg arg = new ConstructorArg();
                    arg.name.set(Reflections.parameterName(parameter));
                    arg.type.set(parameter.getType().getName());
                    newBeanData.constructorArgs.add(arg);
                }
                table.getItems().add(newBeanData);
                newBeanData.updateBeanData(profile);
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

    private List<MenuItem> editors(Class<?> type, BeanData beanData) {
        final List<MenuItem> items = new ArrayList<>();
        final URLClassLoader classLoader = cacheManager.getClassLoader(profile);
        for (final BeanEditor editor : ServiceLoader.load(BeanEditor.class, classLoader)) {
            for (final Class<?> e : editor.getBeanTypes()) {
                if (e.isAssignableFrom(type)) {
                    final MenuItem menuItem = new MenuItem(s(editor.getName()));
                    menuItem.setOnAction(event -> {
                        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
                        context.setClassLoader(classLoader);
                        context.setAllowCircularReferences(false);
                        context.getBeanFactory().addBeanPostProcessor(new WindowAndDialogPostProcessor(context));
                        context.setDisplayName(editor.getName());
                        context.register((Class[]) editor.getConfigurations());
                        context.setParent(BeanEditorActions.this.context);
                        context.getBeanFactory().registerSingleton("beanData", beanData);
                        context.refresh();
                        context.start();
                    });
                    items.add(menuItem);
                }
            }
        }
        return items;
    }

    public ContextMenu contextMenu(BeanData beanData) {
        final ContextMenu menu = new ContextMenu();
        final List<List<MenuItem>> menuItems = new ArrayList<>();
        final Class<?> type = beanData.getClass(profile).orElse(null);
        if (type == null) {
            return menu;
        }

        menuItems.add(factoryItems(type, beanData));
        menuItems.add(editors(type, beanData));

        {
            final MenuItem editItem = new MenuItem(s("Edit..."), FontIcons.glyphIcon(FontIcon.M_EDIT, 16));
            editItem.setOnAction(this::onEdit);
            menuItems.add(Collections.singletonList(editItem));
        }

        {
            final MenuItem removeItem = new MenuItem(s("Remove"), FontIcons.glyphIcon(FontIcon.M_REMOVE, 16));
            removeItem.setOnAction(e -> table.getItems().remove(beanData));
            menuItems.add(Collections.singletonList(removeItem));
        }

        menuItems.removeIf(List::isEmpty);
        for (final ListIterator<List<MenuItem>> i = menuItems.listIterator(); i.hasNext(); ) {
            if (i.hasPrevious()) {
                menu.getItems().add(new SeparatorMenuItem());
            }
            menu.getItems().addAll(i.next());
        }
        return menu;
    }
}
