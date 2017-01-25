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
import org.marid.beans.TypeInfo;
import org.marid.dependant.beaneditor.ValueMenuItems;
import org.marid.ide.project.ProjectProfile;
import org.marid.idefx.controls.IdeShapes;
import org.marid.jfx.menu.MaridContextMenu;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class BeanPropEditor extends TableView<BeanProp> {

    private final BeanData beanData;

    @Autowired
    public BeanPropEditor(BeanData beanData) {
        super(beanData.properties);
        this.beanData = beanData;
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
    public void typeColumn(ProjectProfile profile) {
        final TableColumn<BeanProp, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Type"));
        col.setPrefWidth(250);
        col.setMaxWidth(520);
        col.setCellValueFactory(param -> createObjectBinding(() -> {
            final ResolvableType type = profile.getPropType(beanData, param.getValue().getName());
            return type == ResolvableType.NONE ? "?" : type.toString();
        }, param.getValue().observables()));
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void valueColumn() {
        final TableColumn<BeanProp, Label> col = new TableColumn<>();
        col.textProperty().bind(ls("Value"));
        col.setPrefWidth(500);
        col.setMaxWidth(1500);
        col.setCellValueFactory(p -> {
            final BeanProp prop = p.getValue();
            return createObjectBinding(() -> label(prop.getData()), prop.observables());
        });
        getColumns().add(col);
    }

    public static Label label(DElement<?> element) {
        final Label label = new Label();
        if (element instanceof DRef) {
            final DRef ref = (DRef) element;
            label.setGraphic(IdeShapes.ref(ref, 16));
        } else if (element instanceof DValue) {
            label.setGraphic(glyphIcon(M_TEXT_FORMAT, 16));
        } else if (element instanceof DCollection) {
            label.setGraphic(glyphIcon(M_LIST, 16));
        } else if (element instanceof BeanData) {
            final BeanData data = (BeanData) element;
            label.setGraphic(IdeShapes.beanNode(data, 16));
        } else if (element instanceof DMap) {
            label.setGraphic(glyphIcon(M_MAP, 16));
        }
        if (element != null) {
            label.setText(element.toString());
        }
        return label;
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
                    for (final TypeInfo typeInfo : classInfo.propertyInfos) {
                        if (typeInfo.name.equals(prop.getName())) {
                            editors.add(typeInfo);
                            break;
                        }
                    }
                }
                final ResolvableType type = profile.getPropType(beanData, prop.getName());
                final ValueMenuItems menuItems = new ValueMenuItems(prop.data, type, editors, prop.name);
                factory.initializeBean(menuItems, null);
                menuItems.addTo(m);
            }));
            return row;
        });
    }
}
