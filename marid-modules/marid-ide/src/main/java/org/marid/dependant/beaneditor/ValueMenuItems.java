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

import javafx.beans.value.WritableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.marid.IdeDependants;
import org.marid.beans.TypeInfo;
import org.marid.dependant.beaneditor.listeditor.ListEditorConfiguration;
import org.marid.dependant.beaneditor.propeditor.PropEditorConfiguration;
import org.marid.dependant.beaneditor.valueeditor.ValueEditorConfiguration;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcon;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.collection.DArray;
import org.marid.spring.xml.collection.DElement;
import org.marid.spring.xml.collection.DList;
import org.marid.spring.xml.collection.DValue;
import org.marid.spring.xml.props.DProps;
import org.marid.spring.xml.ref.DRef;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.beans.Introspector.decapitalize;
import static java.lang.reflect.Modifier.*;
import static org.marid.jfx.LocalizedStrings.fls;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueMenuItems {

    private final List<MenuItem> items = new ArrayList<>();
    private final WritableValue<DElement<?>> element;
    private final ResolvableType type;
    private final List<TypeInfo> editors;

    public ValueMenuItems(WritableValue<DElement<?>> element, ResolvableType type, List<TypeInfo> editors) {
        this.element = element;
        this.type = type;
        this.editors = editors;
    }

    @OrderedInit(1)
    public void initClearItem() {
        if (element.getValue() != null) {
            final MenuItem clearItem = new MenuItem(s("Clear value"), glyphIcon(M_CLEAR, 16));
            clearItem.setOnAction(ev -> element.setValue(null));
            items.add(clearItem);
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(2)
    public void initEditValue(IdeDependants dependants) {
        final MenuItem mi = new MenuItem(s("Edit value..."), glyphIcon(M_MODE_EDIT, 16));
        mi.setOnAction(event -> {
            if (!(element.getValue() instanceof DValue)) {
                element.setValue(new DValue());
            }
            dependants.start("valueEditor", ValueEditorConfiguration.class, c -> {
                c.value = (DValue) element.getValue();
                c.type = type;
            });
        });
        items.add(mi);
        items.add(new SeparatorMenuItem());
    }

    @OrderedInit(3)
    public void initRefValue(BeanMetaInfoProvider metaInfoProvider) {
        final List<MenuItem> refItems = new ArrayList<>();
        final BeanMetaInfoProvider.BeansMetaInfo metaInfo = metaInfoProvider.profileMetaInfo();
        for (final BeanDefinitionHolder h : metaInfo.beans(type)) {
            final String name = h.getBeanName();
            final MenuItem item = new MenuItem(name, glyphIcon(FontIcon.M_BEENHERE, 16));
            item.setOnAction(event -> {
                final DRef ref = new DRef();
                ref.setBean(name);
                element.setValue(ref);
            });
            refItems.add(item);
        }
        if (!refItems.isEmpty()) {
            if (refItems.get(refItems.size() - 1) instanceof SeparatorMenuItem) {
                refItems.remove(refItems.size() - 1);
            }
            final Menu menu = new Menu(s("Reference"), glyphIcon(M_LINK, 16));
            menu.getItems().addAll(refItems);
            items.add(menu);
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(4)
    public void initNewBean(BeanMetaInfoProvider provider, BeanListActions actions) {
        final List<MenuItem> refItems = new ArrayList<>();
        final BeanMetaInfoProvider.BeansMetaInfo metaInfo = provider.metaInfo();
        metaInfo.beans(type).forEach(h -> {
            final MenuItem item = new MenuItem(h.getBeanName(), glyphIcon(FontIcon.M_ACCOUNT_BALANCE, 16));
            item.setOnAction(event -> {
                final BeanData data = actions.insertItem(h.getBeanName(), h.getBeanDefinition(), metaInfo);
                final DRef ref = new DRef();
                ref.setBean(data.getName());
                element.setValue(ref);
            });
            refItems.add(item);
        });
        if (!refItems.isEmpty()) {
            if (refItems.get(refItems.size() - 1) instanceof SeparatorMenuItem) {
                refItems.remove(refItems.size() - 1);
            }
            final Menu menu = new Menu(s("New bean"), glyphIcon(M_ACCOUNT_BALANCE, 16));
            menu.getItems().addAll(refItems);
            items.add(menu);
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(5)
    public void initNewBean(ProjectProfile profile, BeanListActions actions) {
        if (type != ResolvableType.NONE) {
            final Class<?> c = type.getRawClass();
            if ((c.getModifiers() & (INTERFACE | PRIVATE | PROTECTED | ABSTRACT)) == 0) {
                final MenuItem item = new MenuItem("New bean from class", glyphIcon(FontIcon.M_ACCOUNT_BALANCE, 16));
                item.setOnAction(event -> {
                    final BeanData data = new BeanData();
                    data.name.setValue(profile.generateBeanName(decapitalize(c.getSimpleName())));
                    data.type.setValue(c.getName());
                    actions.insertItem(data);
                    final DRef ref = new DRef();
                    ref.setBean(data.getName());
                    element.setValue(ref);
                });
                items.add(item);
                items.add(new SeparatorMenuItem());
            }
        }
    }

    @OrderedInit(6)
    public void initPropertiesEdit(IdeDependants dependants) {
        if (ResolvableType.forClass(Properties.class).isAssignableFrom(type)) {
            final MenuItem mi = new MenuItem(s("Edit properties..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(e -> {
                final DProps props;
                if (element.getValue() instanceof DProps) {
                    props = (DProps) element.getValue();
                } else {
                    element.setValue(props = new DProps());
                }
                dependants.start("propsEditor", PropEditorConfiguration.class, c -> c.props = props);
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(7)
    public void initListEdit(IdeDependants dependants) {
        if (ResolvableType.forClass(List.class).isAssignableFrom(type)) {
            final MenuItem mi = new MenuItem(s("Edit list..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(event -> {
                final DList list;
                if (element.getValue() instanceof DList) {
                    list = (DList) element.getValue();
                } else {
                    element.setValue(list = new DList());
                }
                final ResolvableType[] generics = type.as(List.class).getGenerics();
                if (generics.length > 0 && generics[0] != ResolvableType.NONE) {
                    list.valueType.set(generics[0].getRawClass().getName());
                }
                dependants.start("listEditor", ListEditorConfiguration.class, c -> {
                    c.collection = (DList) element.getValue();
                    c.type = type;
                });
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(8)
    public void initArrayEdit(IdeDependants dependants) {
        if (type.isArray()) {
            final MenuItem mi = new MenuItem(s("Edit array..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(event -> {
                final DArray array;
                if (element.getValue() instanceof DArray) {
                    array = (DArray) element.getValue();
                } else {
                    element.setValue(array = new DArray());
                }
                if (type.getComponentType() != ResolvableType.NONE) {
                    array.valueType.setValue(type.getComponentType().getRawClass().getName());
                }
                dependants.start("arrayEditor", ListEditorConfiguration.class, c -> {
                    c.collection = array;
                    c.type = type;
                });
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(9)
    public void initEditor(IdeDependants dependants, ProjectProfile profile) {
        if (editors.isEmpty()) {
            return;
        }
        for (final TypeInfo editor : editors) {
            if (editor.editors.isEmpty()) {
                continue;
            }
            final MenuItem menuItem = new MenuItem();
            menuItem.textProperty().bind(fls("Edit: %s", editor.title == null ? editor.name : editor.title));
            menuItem.setOnAction(event -> {
                final Class<?>[] classes = editor.editors.toArray(new Class<?>[editor.editors.size()]);
                dependants.start("editor", context -> {
                    context.setClassLoader(profile.getClassLoader());
                    context.register(classes);
                    context.getBeanFactory().registerSingleton("element", element);
                    context.getBeanFactory().registerSingleton("editor", editor);
                    context.getBeanFactory().registerSingleton("valueType", type);
                });
            });
            items.add(menuItem);
        }
    }

    public void addTo(ContextMenu contextMenu) {
        addTo(contextMenu.getItems());
    }

    public void addTo(Menu menu) {
        addTo(menu.getItems());
    }

    public void addTo(List<MenuItem> menuItems) {
        if (items.isEmpty()) {
            return;
        }
        if (!menuItems.isEmpty()) {
            menuItems.add(new SeparatorMenuItem());
        }
        if (!items.isEmpty()) {
            if (items.get(items.size() - 1) instanceof SeparatorMenuItem) {
                items.remove(items.size() - 1);
            }
        }
        menuItems.addAll(items);
    }
}
