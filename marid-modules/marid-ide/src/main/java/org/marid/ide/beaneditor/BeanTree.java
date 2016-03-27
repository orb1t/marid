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

package org.marid.ide.beaneditor;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.ConstructorArg;
import org.marid.ide.beaneditor.data.Property;
import org.marid.ide.beaneditor.ui.NameCell;
import org.marid.ide.beaneditor.ui.NameFactory;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.misc.Casts;
import org.marid.xml.IterableNodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

/**
 * @author Dmitry Ovchinnikov
 */
class BeanTree extends TreeTableView<Object> implements LogSupport, L10nSupport {

    static final Image ROOT = new Image("http://icons.iconarchive.com/icons/martz90/circle-addon1/24/root-explorer-icon.png", true);
    static final Image DIR = new Image("http://icons.iconarchive.com/icons/hopstarter/mac-folders-2/24/Folder-Download-icon.png", true);
    static final Image BEAN = new Image("http://icons.iconarchive.com/icons/oxygen-icons.org/oxygen/24/Apps-java-icon.png", true);
    static final Image PROP = new Image("http://icons.iconarchive.com/icons/icons8/windows-8/24/Programming-Edit-Property-icon.png", true);
    static final Image CPARAM = new Image("http://icons.iconarchive.com/icons/custom-icon-design/flatastic-6/24/Circle-icon.png", true);

    private final BeanEditor beanEditor;

    BeanTree(ProjectProfile rootObject, BeanEditor beanEditor) {
        super(new TreeItem<>(rootObject, new ImageView(ROOT)));
        this.beanEditor = beanEditor;
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
        getColumns().add(nameColumn());
        setTreeColumn(getColumns().get(0));
        setEditable(true);
        setRowFactory(param -> {
            final TreeTableRow<Object> row = new TreeTableRow<>();
            row.setPrefHeight(30);
            return row;
        });
    }

    ProjectProfile getProfile() {
        return (ProjectProfile) getRoot().getValue();
    }

    private TreeTableColumn<Object, String> nameColumn() {
        final TreeTableColumn<Object, String> column = new TreeTableColumn<>(s("Name"));
        column.setCellValueFactory(new NameFactory());
        column.setCellFactory(NameCell::new);
        column.setPrefWidth(300);
        column.setEditable(true);
        return column;
    }

    void update(ProjectProfile profile) {
        setRoot(new TreeItem<>(profile, new ImageView(ROOT)));
        try {
            final Path beansDirectory = profile.getBeansDirectory();
            try (final Stream<Path> stream = Files.walk(beansDirectory)) {
                final Set<Path> set = stream
                        .filter(p -> p.getFileName().toString().endsWith(".xml"))
                        .collect(Collectors.toCollection(TreeSet::new));
                for (final Path xml : set) {
                    final TreeItem<Object> node = stream(beansDirectory.relativize(xml).spliterator(), false)
                            .reduce(getRoot(), (a, p) -> {
                                final Path parentPath = a == getRoot() ? beansDirectory : (Path) a.getValue();
                                final Path currentPath = parentPath.resolve(p);
                                final TreeItem<Object> child = new TreeItem<>(currentPath, new ImageView(DIR));
                                a.getChildren().add(child);
                                return child;
                            }, (i1, i2) -> i2);
                    loadBeans(node, xml);
                }
            }
        } catch (Exception x) {
            log(WARNING, "Unable to load beans", x);
        }
    }

    void loadBeans(TreeItem<Object> parent, Path xmlFile) throws Exception {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setCoalescing(true);
        documentBuilderFactory.setNamespaceAware(true);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(xmlFile.toUri().toASCIIString());
        final Element root = document.getDocumentElement();
        new IterableNodeList(root.getChildNodes()).stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .filter(e -> e.getNodeName().equals("bean"))
                .forEach(bean -> {
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
                    final Map<String, TreeItem<Property>> propertyMap = new TreeMap<>();
                    final Map<String, TreeItem<ConstructorArg>> constructorArgMap = new TreeMap<>();
                    final ClassData classData = beanEditor.classData(beanData.type.get());
                    classData.getSetters().forEach((name, method) -> {
                        final String type = method.getParameterTypes()[0].getName();
                        final Image img = beanEditor.image(type);
                        final Node icon = img != null ? new ImageView(img) : new ImageView(PROP);
                        final Property property = new Property();
                        property.name.set(name);
                        final TreeItem<Property> item = new TreeItem<>(property, icon);
                        propertyMap.put(name, item);
                    });
                    classData.getParameters().forEach((name, parameter) -> {
                        final String type = parameter.getType().getName();
                        final Image img = beanEditor.image(type);
                        final Node icon = img != null ? new ImageView(img) : new ImageView(CPARAM);
                        final ConstructorArg constructorArg = new ConstructorArg();
                        constructorArg.name.set(name);
                        final TreeItem<ConstructorArg> item = new TreeItem<>(constructorArg, icon);
                        constructorArgMap.put(name, item);
                    });
                    new IterableNodeList(bean.getChildNodes()).stream()
                            .filter(Element.class::isInstance)
                            .map(Element.class::cast)
                            .filter(e -> "constructor-arg".equals(e.getNodeName()))
                            .filter(e -> e.hasAttribute("name"))
                            .filter(e -> constructorArgMap.containsKey(e.getAttribute("name")))
                            .forEach(e -> {
                                final TreeItem<ConstructorArg> item = constructorArgMap.get(e.getAttribute("name"));
                                item.getValue().value.set(e.getAttribute("value"));
                                item.getValue().ref.set(e.getAttribute("ref"));
                            });
                    new IterableNodeList(bean.getChildNodes()).stream()
                            .filter(Element.class::isInstance)
                            .map(Element.class::cast)
                            .filter(e -> "property".equals(e.getNodeName()))
                            .filter(e -> e.hasAttribute("name"))
                            .filter(e -> propertyMap.containsKey(e.getAttribute("name")))
                            .forEach(e -> {
                                final TreeItem<Property> item = propertyMap.get(e.getAttribute("name"));
                                item.getValue().value.set(e.getAttribute("value"));
                                item.getValue().ref.set(e.getAttribute("ref"));
                            });
                    constructorArgMap.forEach((name, item) -> beanItem.getChildren().add(Casts.cast(item)));
                    propertyMap.forEach((name, item) -> beanItem.getChildren().add(Casts.cast(item)));
                });
    }
}
