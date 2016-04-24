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

package org.marid.ide.beaneditor.data;

import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.marid.ide.beaneditor.BeanEditor;
import org.marid.ide.beaneditor.BeanTreeConstants;
import org.marid.ide.beaneditor.ClassData;
import org.marid.ide.project.ProjectProfile;
import org.marid.xml.IterableNodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class Loader implements BeanTreeConstants {

    private final BeanEditor beanEditor;

    public Loader(BeanEditor beanEditor) {
        this.beanEditor = beanEditor;
    }

    public void load(TreeItem<Object> root) throws Exception {
        final ProjectProfile profile = (ProjectProfile) root.getValue();
        final Path beansDirectory = profile.getBeansDirectory();
        try (final Stream<Path> stream = Files.walk(beansDirectory)) {
            final Set<Path> set = stream
                    .filter(p -> p.getFileName().toString().endsWith(".xml"))
                    .collect(Collectors.toCollection(TreeSet::new));
            for (final Path xml : set) {
                final TreeItem<Object> node = stream(beansDirectory.relativize(xml).spliterator(), false)
                        .reduce(root, (a, p) -> {
                            final Path parentPath = a == root ? beansDirectory : (Path) a.getValue();
                            final Path currentPath = parentPath.resolve(p);
                            final Image image = Files.isDirectory(currentPath) ? DIR : FILE;
                            final TreeItem<Object> child = new TreeItem<>(currentPath, new ImageView(image));
                            child.setExpanded(true);
                            a.getChildren().add(child);
                            return child;
                        }, (i1, i2) -> i2);
                loadBeans(root, node, xml);
            }
        }
        root.setExpanded(true);
    }

    private void loadBeans(TreeItem<Object> root, TreeItem<Object> parent, Path xmlFile) throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setCoalescing(true);
        documentBuilderFactory.setNamespaceAware(true);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(xmlFile.toUri().toASCIIString());
        final Element docElement = document.getDocumentElement();
        new IterableNodeList(docElement.getChildNodes()).stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .filter(e -> e.getNodeName().equals("bean"))
                .forEach(bean -> loadBean(parent, bean));
        bindReferences(root);
    }

    private void loadBean(TreeItem<Object> parent, Element bean) {
        final BeanData beanData = new BeanData();
        beanData.name.set(bean.getAttribute("name"));
        beanData.type.set(bean.getAttribute("class"));
        beanData.destroyMethod.set(bean.getAttribute("destroy-method"));
        beanData.initMethod.set(bean.getAttribute("init-method"));
        beanData.factoryBean.set(bean.getAttribute("factory-bean"));
        beanData.factoryMethod.set(bean.getAttribute("factory-method"));
        beanData.lazyInit.set(bean.getAttribute("lazy-init"));
        final Image image = beanEditor.image(beanData.type.get());
        final Node beanIcon = image != null ? new ImageView(image) : new ImageView(BEAN);
        final TreeItem<Object> beanItem = new TreeItem<>(beanData, beanIcon);
        parent.getChildren().add(beanItem);
        final ClassData classData = beanEditor.classData(beanData.type.get());
        fillProperties(classData, beanItem, bean);
        fillConstructorArg(classData, beanItem, bean);
    }

    private void setProperties(String name, Element element, String elementName, StringProperty... properties) {
        new IterableNodeList(element.getChildNodes()).stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .filter(e -> elementName.equals(e.getNodeName()) && name.equals(e.getAttribute("name")))
                .forEach(e -> {
                    for (final StringProperty property : properties) {
                        if (e.hasAttribute(property.getName())) {
                            property.set(e.getAttribute(property.getName()));
                        }
                    }
                });
    }

    public void fillProperties(ClassData classData, TreeItem<Object> treeItem, Element bean) {
        classData.getSetters().forEach((name, method) -> {
            final String type = method.getParameterTypes()[0].getName();
            final Image img = beanEditor.image(type);
            final Node icon = img != null ? new ImageView(img) : new ImageView(PROP);
            final Property property = new Property();
            property.name.set(name);
            property.type.set(type);
            if (bean != null) {
                setProperties(name, bean, "property", property.ref, property.value);
            }
            mutuallyExcludeProperties(property.ref, property.value);
            treeItem.getChildren().add(new TreeItem<>(property, icon));
        });
    }

    public void fillConstructorArg(ClassData classData, TreeItem<Object> treeItem, Element bean) {
        classData.getParameters().forEach((name, parameter) -> {
            final String type = parameter.getType().getName();
            final Image img = beanEditor.image(type);
            final Node icon = img != null ? new ImageView(img) : new ImageView(CPARAM);
            final ConstructorArg constructorArg = new ConstructorArg();
            constructorArg.name.set(name);
            constructorArg.type.set(type);
            if (bean != null) {
                setProperties(name, bean, "constructor-arg", constructorArg.ref, constructorArg.value);
            }
            mutuallyExcludeProperties(constructorArg.ref, constructorArg.value);
            treeItem.getChildren().add(new TreeItem<>(constructorArg, icon));
        });
    }

    private void mutuallyExcludeProperties(StringProperty p1, StringProperty p2) {
        p1.addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isNotBlank(newValue)) {
                p2.set(null);
            }
        });
        p2.addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isNotBlank(newValue)) {
                p1.set(null);
            }
        });
    }

    private void bindReferences(TreeItem<Object> root) {
        final Map<String, BeanData> beanDataMap = new HashMap<>();
        final List<ConstructorArg> constructorArgs = new ArrayList<>();
        final List<Property> properties = new ArrayList<>();
        final AtomicReference<Consumer<TreeItem<Object>>> ref = new AtomicReference<>();
        ref.set(item -> {
            final Consumer<TreeItem<Object>> self = ref.get();
            if (item.getValue() instanceof BeanData) {
                final BeanData data = (BeanData) item.getValue();
                beanDataMap.put(data.name.get(), data);
            } else if (item.getValue() instanceof ConstructorArg) {
                constructorArgs.add((ConstructorArg) item.getValue());
            } else if (item.getValue() instanceof Property) {
                properties.add((Property) item.getValue());
            }
            item.getChildren().forEach(self);
        });
        ref.get().accept(root);
        constructorArgs.forEach(constructorArg -> {
            final BeanData beanData = beanDataMap.get(constructorArg.ref.get());
            if (beanData != null) {
                constructorArg.ref.bind(beanData.name);
            }
        });
        properties.forEach(property -> {
            final BeanData beanData = beanDataMap.get(property.ref.get());
            if (beanData != null) {
                property.ref.bind(beanData.name);
            }
        });
        beanDataMap.values().forEach(data -> {
            final BeanData beanData = beanDataMap.get(data.factoryBean.get());
            if (beanData != null) {
                data.factoryBean.bind(beanData.name);
            }
        });
    }
}
