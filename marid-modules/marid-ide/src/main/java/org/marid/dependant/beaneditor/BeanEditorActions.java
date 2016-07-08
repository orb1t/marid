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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import org.marid.IdeDependants;
import org.marid.dependant.beandata.BeanDataEditorConfiguration;
import org.marid.ide.project.ProjectCacheManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.ConstructorArg;
import org.marid.spring.xml.data.Property;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorActions {

    private final BeanEditorTable table;
    private final ProjectCacheManager cacheManager;
    private final ObjectFactory<Dialog<Map.Entry<String, BeanDefinition>>> beanBrowser;
    private final IdeDependants dependants;
    private final ProjectProfile profile;

    public final BooleanBinding itemActionDisabled;
    public final BooleanBinding clearDisabled;

    @Autowired
    public BeanEditorActions(BeanEditorTable table,
                             ProjectCacheManager cacheManager,
                             ObjectFactory<Dialog<Map.Entry<String, BeanDefinition>>> beanBrowser,
                             IdeDependants dependants,
                             ProjectProfile profile) {
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
        final Optional<Map.Entry<String, BeanDefinition>> entry = beanBrowser.getObject().showAndWait();
        if (entry.isPresent()) {
            final BeanData beanData = new BeanData();
            final BeanDefinition def = entry.get().getValue();
            beanData.name.set(cacheManager.generateBeanName(profile, entry.get().getKey()));
            beanData.factoryBean.set(def.getFactoryBeanName());
            beanData.factoryMethod.set(def.getFactoryMethodName());
            beanData.type.set(def.getBeanClassName());
            beanData.lazyInit.set(def.isLazyInit() ? "true" : null);

            if (entry.get().getValue() instanceof AbstractBeanDefinition) {
                final AbstractBeanDefinition definition = (AbstractBeanDefinition) entry.get().getValue();
                beanData.initMethod.set(definition.getInitMethodName());
                beanData.destroyMethod.set(definition.getDestroyMethodName());
            }

            if (def.getConstructorArgumentValues() != null) {
                for (final ConstructorArgumentValues.ValueHolder holder : def.getConstructorArgumentValues().getGenericArgumentValues()) {
                    final ConstructorArg constructorArg = new ConstructorArg();
                    constructorArg.name.set(holder.getName());
                    constructorArg.type.set(holder.getType());
                    constructorArg.value.set(holder.getValue() == null ? null : holder.getValue().toString());
                    beanData.constructorArgs.add(constructorArg);
                }
            }

            if (def.getPropertyValues() != null) {
                for (final PropertyValue propertyValue : def.getPropertyValues().getPropertyValueList()) {
                    final Property property = new Property();
                    property.name.set(propertyValue.getName());
                    property.value.set(propertyValue.getValue() == null ? null : propertyValue.getValue().toString());
                    beanData.properties.add(property);
                }
            }

            cacheManager.updateBeanData(profile, beanData);

            table.getItems().add(beanData);
        }
    }

    public void onShowPopup(ActionEvent event) {
        final Node node = (Node) event.getSource();
        final Side side = node instanceof Button ? Side.BOTTOM : Side.RIGHT;
        contextMenu(table.getSelectionModel().getSelectedItem()).show(node, side, 5, 5);
    }

    public ContextMenu contextMenu(BeanData beanData) {
        final ContextMenu contextMenu = new ContextMenu();
        final Class<?> type = cacheManager.getBeanClass(profile, beanData).orElse(null);

        if (type != null) {
            for (final Method method : type.getMethods()) {
                if (method.getReturnType() == void.class || method.getDeclaringClass() == Object.class) {
                    continue;
                }
                final String name = Stream.of(method.getParameters())
                        .map(p -> p.getParameterizedType().toString())
                        .collect(Collectors.joining(",", method.getName() + "(", ") : " + method.getGenericReturnType()));
                final MenuItem menuItem = new MenuItem(name);
                menuItem.setOnAction(ev -> {
                    final BeanData newBeanData = new BeanData();
                    newBeanData.name.set(method.getName());
                    newBeanData.factoryBean.set(beanData.name.get());
                    newBeanData.factoryMethod.set(method.getName());
                    cacheManager.updateBeanData(profile, newBeanData);
                    table.getItems().add(newBeanData);
                });
                contextMenu.getItems().add(menuItem);
            }
        }
        return contextMenu;
    }
}
