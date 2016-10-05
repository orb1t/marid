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

import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.BeanBrowserTable.BeanBrowserItem;
import org.marid.dependant.beaneditor.beandata.BeanDataEditorConfiguration;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.project.ProjectProfileReflection;
import org.marid.jfx.dialog.MaridDialog;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.misc.Reflections;
import org.marid.spring.beandata.BeanEditor;
import org.marid.spring.postprocessors.WindowAndDialogPostProcessor;
import org.marid.spring.xml.data.BeanArg;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.BeanProp;
import org.marid.spring.xml.data.collection.DValue;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static org.marid.Ide.primaryStage;
import static org.marid.jfx.icons.FontIcon.M_EDIT;
import static org.marid.jfx.icons.FontIcon.M_REMOVE;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanListActions {

    private final ApplicationContext context;
    private final BeanListTable table;
    private final IdeDependants dependants;
    private final ProjectProfile profile;
    private final ProjectProfileReflection reflection;

    @Autowired
    public BeanListActions(ApplicationContext context,
                           BeanListTable table,
                           IdeDependants dependants,
                           ProjectProfile profile,
                           ProjectProfileReflection reflection) {
        this.context = context;
        this.table = table;
        this.dependants = dependants;
        this.profile = profile;
        this.reflection = reflection;
    }

    public void onEdit(ActionEvent event) {
        dependants.start("beanDataEditor", builder -> builder
                .conf(BeanDataEditorConfiguration.class)
                .arg("beanData", table.getSelectionModel().getSelectedItem()));
    }

    public void onDelete(ActionEvent event) {
        table.getItems().remove(table.getSelectionModel().getSelectedIndex());
    }

    public void onClear(ActionEvent event) {
        table.getItems().clear();
    }

    public void onBrowse(ActionEvent event) {
        final BeanBrowserTable beans = context.getBean(BeanBrowserTable.class);
        new MaridDialog<List<BeanBrowserItem>>(primaryStage, new ButtonType(s("Add"), OK_DONE), CANCEL)
                .preferredSize(1024, 768)
                .title("Bean browser")
                .with((d, p) -> d.setResizable(true))
                .result(beans.getSelectionModel()::getSelectedItems)
                .with((d, p) -> p.setContent(new MaridScrollPane(beans)))
                .showAndWait()
                .ifPresent(entries -> entries.forEach(e -> insertItem(e.name, e.definition, e.metaInfo)));
    }

    public void onAddNew(ActionEvent event) {
        final BeanData beanData = new BeanData();
        final String name = profile.generateBeanName("newBean");
        beanData.name.set(name);
        beanData.type.set(Object.class.getName());
        table.getItems().add(beanData);
    }

    public BeanData beanData(String name, BeanDefinition def) {
        final BeanData beanData = new BeanData();
        beanData.name.set(profile.generateBeanName(name));
        beanData.factoryBean.set(def.getFactoryBeanName());
        beanData.factoryMethod.set(def.getFactoryMethodName());
        beanData.type.set(def.getBeanClassName());
        beanData.lazyInit.set(def.isLazyInit() ? "true" : null);

        if (def instanceof AbstractBeanDefinition) {
            final AbstractBeanDefinition definition = (AbstractBeanDefinition) def;
            beanData.initMethod.set(definition.getInitMethodName());
            beanData.destroyMethod.set(definition.getDestroyMethodName());
        }

        if (def.getConstructorArgumentValues() != null) {
            for (final ValueHolder holder : def.getConstructorArgumentValues().getGenericArgumentValues()) {
                final BeanArg beanArg = new BeanArg();
                beanArg.name.set(holder.getName());
                beanArg.type.set(holder.getType());
                if (holder.getValue() instanceof TypedStringValue) {
                    final DValue value = new DValue();
                    value.setValue(((TypedStringValue) holder.getValue()).getValue());
                    beanArg.data.set(value);
                }
                beanData.beanArgs.add(beanArg);
            }
        }

        if (def.getPropertyValues() != null) {
            for (final PropertyValue propertyValue : def.getPropertyValues().getPropertyValueList()) {
                final BeanProp property = new BeanProp();
                property.name.set(propertyValue.getName());
                if (propertyValue.getValue() instanceof TypedStringValue) {
                    final DValue value = new DValue();
                    value.setValue(((TypedStringValue) propertyValue.getValue()).getValue());
                    property.data.set(value);
                }
                beanData.properties.add(property);
            }
        }

        reflection.updateBeanData(beanData);
        return beanData;
    }

    public BeanData insertItem(String name, BeanDefinition def, BeanMetaInfoProvider.BeansMetaInfo metaInfo) {
        final BeanData beanData = beanData(name, def);
        reflection.updateBeanData(beanData);
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
                    final BeanArg arg = new BeanArg();
                    arg.name.set(Reflections.parameterName(parameter));
                    arg.type.set(parameter.getType().getName());
                    newBeanData.beanArgs.add(arg);
                }
                table.getItems().add(newBeanData);
                reflection.updateBeanData(newBeanData);
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
        final URLClassLoader classLoader = profile.getClassLoader();
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
                        context.setParent(BeanListActions.this.context);
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
        final Class<?> type = reflection.getClass(beanData).orElse(null);
        if (type == null) {
            return menu;
        }

        menuItems.add(factoryItems(type, beanData));
        menuItems.add(editors(type, beanData));

        {
            final MenuItem editItem = new MenuItem(s("Edit..."), glyphIcon(M_EDIT, 16));
            editItem.setOnAction(this::onEdit);
            menuItems.add(Collections.singletonList(editItem));
        }

        {
            final MenuItem removeItem = new MenuItem(s("Remove"), glyphIcon(M_REMOVE, 16));
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
