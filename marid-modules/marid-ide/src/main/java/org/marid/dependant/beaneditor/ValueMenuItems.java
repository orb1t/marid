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

import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.WritableValue;
import javafx.scene.control.*;
import org.apache.commons.lang3.ArrayUtils;
import org.marid.IdeDependants;
import org.marid.beans.TypeInfo;
import org.marid.dependant.beaneditor.BeanMetaInfoProvider.BeansMetaInfo;
import org.marid.dependant.beaneditor.beandata.BeanDataEditorConfiguration;
import org.marid.dependant.beaneditor.beandata.BeanDataEditorParams;
import org.marid.dependant.beaneditor.listeditor.ListEditorConfiguration;
import org.marid.dependant.beaneditor.listeditor.ListEditorParams;
import org.marid.dependant.beaneditor.mapeditor.MapEditorConfiguration;
import org.marid.dependant.beaneditor.mapeditor.MapEditorParams;
import org.marid.dependant.beaneditor.propeditor.PropEditorConfiguration;
import org.marid.dependant.beaneditor.propeditor.PropEditorParams;
import org.marid.dependant.beaneditor.valueeditor.ValueEditorConfiguration;
import org.marid.dependant.beaneditor.valueeditor.ValueEditorParams;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcon;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.contexts.ValueEditorContext;
import org.marid.spring.xml.*;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import static java.beans.Introspector.decapitalize;
import static java.lang.reflect.Modifier.*;
import static org.marid.jfx.LocalizedStrings.fls;
import static org.marid.jfx.LocalizedStrings.ls;
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
    private final List<TypeInfo> editors = new ArrayList<>();
    private final ObservableStringValue name;

    public ValueMenuItems(WritableValue<DElement<?>> element, ResolvableType type, ObservableStringValue name) {
        this.element = element;
        this.type = type;
        this.name = name;
    }

    public void addEditor(TypeInfo typeInfo) {
        editors.add(typeInfo);
    }

    private <T extends DElement<?>> T value(Class<T> type, Supplier<T> supplier) {
        if (type.isInstance(element.getValue())) {
            return type.cast(element.getValue());
        } else {
            final T value = supplier.get();
            element.setValue(value);
            return value;
        }
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
            final DValue value = value(DValue.class, DValue::new);
            dependants.start(ValueEditorConfiguration.class, new ValueEditorParams(type, value), context -> {
                context.setId("valueEditor");
                context.setDisplayName("Value Editor");
            });
        });
        items.add(mi);
        items.add(new SeparatorMenuItem());
    }

    @OrderedInit(3)
    public void initRefValue(BeanMetaInfoProvider metaInfoProvider) {
        MenuItem[] refItems = new MenuItem[0];
        final BeansMetaInfo metaInfo = metaInfoProvider.profileMetaInfo();
        for (final BeanDefinitionHolder h : metaInfo.beans(type)) {
            final String name = h.getBeanName();
            final MenuItem item = new MenuItem(name, glyphIcon(FontIcon.M_BEENHERE, 16));
            item.setOnAction(event -> element.setValue(new DRef(name)));
            refItems = ArrayUtils.add(refItems, item);
        }
        if (refItems.length > 0) {
            items.add(new Menu(s("Reference"), glyphIcon(M_LINK, 16), refItems));
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(4)
    public void initNewBean(BeanMetaInfoProvider provider, BeanListActions actions) {
        MenuItem[] refItems = new MenuItem[0];
        MenuItem[] embItems = new MenuItem[0];
        final BeansMetaInfo metaInfo = provider.metaInfo();
        for (final BeanDefinitionHolder h : metaInfo.beans(type)) {
            final MenuItem refItem = new MenuItem(h.getBeanName(), glyphIcon(M_ACCOUNT_BALANCE, 16));
            refItem.setOnAction(event -> {
                final BeanData data = actions.insertItem(h.getBeanName(), h.getBeanDefinition(), metaInfo);
                element.setValue(new DRef(data.getName()));
            });
            refItems = ArrayUtils.add(refItems, refItem);

            final MenuItem embItem = new MenuItem(h.getBeanName(), glyphIcon(D_ARCHIVE, 16));
            embItem.setOnAction(event -> {
                final BeanData data = actions.beanData(h.getBeanName(), h.getBeanDefinition(), metaInfo, false);
                element.setValue(data);
            });
            embItems = ArrayUtils.add(embItems, embItem);
        }
        if (refItems.length > 0) {
            items.add(new Menu(s("New bean from template"), glyphIcon(M_ACCOUNT_BALANCE, 16), refItems));
            items.add(new Menu(s("New in-place bean from template"), glyphIcon(D_ARCHIVE, 16), embItems));
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(5)
    public void initNewBean(ProjectProfile profile, BeanListActions actions) {
        final Class<?> c = type.getRawClass();
        if (c == null || (c.getModifiers() & (INTERFACE | PRIVATE | PROTECTED | ABSTRACT)) != 0) {
            return;
        }
        {
            final MenuItem item = new MenuItem("New bean from class", glyphIcon(M_ACCOUNT_BALANCE, 16));
            item.setOnAction(event -> {
                final BeanData data = new BeanData();
                data.name.setValue(profile.generateBeanName(decapitalize(c.getSimpleName())));
                data.type.setValue(c.getName());
                actions.insertItem(data);
                element.setValue(new DRef(data.getName()));
            });
            items.add(item);
        }
        {
            final MenuItem item = new MenuItem("New in-place bean", glyphIcon(M_ACCOUNT_BALANCE, 16));
            item.setOnAction(event -> {
                final BeanData data = new BeanData();
                data.name.setValue(profile.generateBeanName(decapitalize(c.getSimpleName())));
                data.type.setValue(c.getName());
                element.setValue(data);
            });
            items.add(item);
        }
        items.add(new SeparatorMenuItem());
    }

    @OrderedInit(6)
    public void initPropertiesEdit(IdeDependants dependants) {
        if (ResolvableType.forClass(Properties.class).isAssignableFrom(type)) {
            final MenuItem mi = new MenuItem(s("Edit properties..."), glyphIcon(M_MODE_EDIT, 16));
            mi.setOnAction(e -> {
                final DProps props = value(DProps.class, DProps::new);
                dependants.start(PropEditorConfiguration.class, new PropEditorParams(props), context -> {
                    context.setId("propEditor");
                    context.setDisplayName("Properties Editor");
                });
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
                final DList list = value(DList.class, DList::new);
                final ResolvableType arg = type.as(List.class).getGeneric(0);
                if (arg != ResolvableType.NONE) {
                    list.valueType.set(arg.getRawClass().getName());
                }
                dependants.start(ListEditorConfiguration.class, new ListEditorParams(type, list, name), context -> {
                    context.setId("listEditor");
                    context.setDisplayName("List Editor");
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
                final DArray array = value(DArray.class, DArray::new);
                if (type.getComponentType() != ResolvableType.NONE) {
                    array.valueType.setValue(type.getComponentType().getRawClass().getName());
                }
                dependants.start(ListEditorConfiguration.class, new ListEditorParams(type, array, name), context -> {
                    context.setId("arrayEditor");
                    context.setDisplayName("Array Editor");
                });
            });
            items.add(mi);
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(9)
    public void initMapEdit(IdeDependants dependants) {
        if (ResolvableType.forClass(Map.class).isAssignableFrom(type)) {
            final MenuItem mi = new MenuItem(s("Edit map..."), glyphIcon(M_PARTY_MODE, 16));
            mi.setOnAction(event -> {
                final DMap map = value(DMap.class, DMap::new);
                final ResolvableType[] generics = type.as(Map.class).getGenerics();
                if (generics.length == 2) {
                    map.setKeyType(generics[0].getRawClass().getName());
                    map.setValueType(generics[1].getRawClass().getName());
                }
                final MapEditorParams params = new MapEditorParams(generics[0], generics[1], map, name);
                dependants.start(MapEditorConfiguration.class, params, c -> {
                    c.setId("mapEditor");
                    c.setDisplayName("Map Editor");
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
        final int size = items.size();
        for (final TypeInfo editor : editors) {
            if (editor.editors.isEmpty()) {
                continue;
            }
            final MenuItem menuItem = new MenuItem();
            menuItem.textProperty().bind(fls("Edit: %s", editor.title == null ? editor.name : editor.title));
            menuItem.setOnAction(event -> {
                final Class<?>[] classes = editor.editors.toArray(new Class<?>[editor.editors.size()]);
                dependants.start(context -> {
                    context.setId("editor");
                    context.setDisplayName("Value Editor");
                    context.setClassLoader(profile.getClassLoader());
                    context.register(classes);
                    context.getBeanFactory().registerSingleton("$ctx", new ValueEditorContext(element, editor, type));
                });
            });
            items.add(menuItem);
        }
        if (items.size() > size) {
            items.add(new SeparatorMenuItem());
        }
    }

    @OrderedInit(10)
    public void initBeanEditor(IdeDependants dependants) {
        if (!(element.getValue() instanceof BeanData)) {
            return;
        }
        final BeanData beanData = (BeanData) element.getValue();
        {
            final BeanDataEditorParams params = new BeanDataEditorParams(beanData);
            final MenuItem menuItem = new MenuItem();
            menuItem.textProperty().bind(ls("Edit bean"));
            menuItem.setOnAction(event -> dependants.start(BeanDataEditorConfiguration.class, params, c -> {
                c.setId("beanDataEditor");
                c.setDisplayName("Bean Data Editor");
            }));
            items.add(menuItem);
        }
        {
            final MenuItem menuItem = new MenuItem();
            menuItem.textProperty().bind(ls("Rename bean name"));
            menuItem.setOnAction(event -> {
                final TextInputDialog dialog = new TextInputDialog(beanData.getName());
                dialog.setTitle(s("Rename bean name"));
                dialog.setHeaderText(s("New bean name") + ":");
                dialog.showAndWait().map(String::trim).filter(s -> !s.isEmpty()).ifPresent(beanData::setName);
            });
            items.add(menuItem);
        }
        items.add(new SeparatorMenuItem());
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
