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

package org.marid.dependant.beaneditor.beandata;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.marid.beans.BeanIntrospector;
import org.marid.beans.ClassInfo;
import org.marid.beans.MethodInfo;
import org.marid.beans.TypeInfo;
import org.marid.dependant.beaneditor.ValueMenuItems;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DCollection;
import org.marid.spring.xml.collection.DElement;
import org.marid.spring.xml.collection.DValue;
import org.marid.spring.xml.ref.DRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BeanPropEditor extends TableView<BeanProp> {

    private final BeanData beanData;
    private final boolean args;

    public BeanPropEditor(BeanData beanData, boolean args) {
        super(args ? beanData.beanArgs : beanData.properties);
        this.beanData = beanData;
        this.args = args;
        setEditable(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
    }

    @OrderedInit(1)
    public void nameColumn() {
        final TableColumn<BeanProp, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Name"));
        col.setPrefWidth(200);
        col.setMaxWidth(400);
        col.setCellValueFactory(param -> param.getValue().name);
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void typeColumn() {
        final TableColumn<BeanProp, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Type"));
        col.setPrefWidth(250);
        col.setMaxWidth(520);
        col.setCellValueFactory(param -> param.getValue().type);
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void valueColumn() {
        final TableColumn<BeanProp, Label> col = new TableColumn<>();
        col.textProperty().bind(ls("Value"));
        col.setPrefWidth(500);
        col.setMaxWidth(1500);
        col.setCellValueFactory(param -> createObjectBinding(() -> {
            final Label label = new Label();
            final DElement<?> element = param.getValue().getData();
            if (element instanceof DRef) {
                label.setGraphic(glyphIcon(M_LINK, 16));
            } else if (element instanceof DValue) {
                label.setGraphic(glyphIcon(M_TEXT_FORMAT, 16));
            } else if (element instanceof DCollection) {
                label.setGraphic(glyphIcon(M_LIST, 16));
            } else if (element instanceof BeanData) {
                label.setGraphic(glyphIcon(M_ALBUM, 16));
            }
            if (element != null) {
                label.setText(element.toString());
            }
            return label;
        }, param.getValue()));
        getColumns().add(col);
    }

    @Autowired
    public void initContextMenu(ProjectProfile profile, AutowireCapableBeanFactory factory) {
        setRowFactory(param -> {
            final TableRow<BeanProp> row = new TableRow<>();
            row.disableProperty().bind(row.itemProperty().isNull());
            row.setContextMenu(new MaridContextMenu(m -> {
                m.getItems().clear();
                final BeanProp prop = row.getItem();
                if (prop == null) {
                    return;
                }
                final ResolvableType beanType = profile.getType(beanData);
                final List<TypeInfo> editors = new ArrayList<>();
                for (final ClassInfo classInfo : BeanIntrospector.classInfos(profile.getClassLoader(), beanType)) {
                    if (args) {
                        final Executable c = profile.getConstructor(beanData).orElse(null);
                        for (final MethodInfo methodInfo : classInfo.constructorInfos) {
                            if (methodInfo.matches(c)) {
                                for (final TypeInfo typeInfo : methodInfo.parameters) {
                                    if (typeInfo.name.equals(prop.getName())) {
                                        editors.add(typeInfo);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        for (final TypeInfo typeInfo : classInfo.propertyInfos) {
                            if (typeInfo.name.equals(prop.getName())) {
                                editors.add(typeInfo);
                                break;
                            }
                        }
                    }
                }
                final ResolvableType type = args
                        ? profile.getArgType(beanData, prop.getName())
                        : profile.getPropType(beanData, prop.getName());
                final ValueMenuItems menuItems = new ValueMenuItems(prop.data, type, editors);
                factory.initializeBean(menuItems, null);
                menuItems.addTo(m);
            }));
            return row;
        });
    }
}
