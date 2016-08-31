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

package org.marid.dependant.beaneditor.beans.listeditor;

import javafx.beans.InvalidationListener;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.common.ValueMenuItems;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.xml.data.AbstractData;
import org.marid.spring.xml.data.collection.DCollection;
import org.marid.spring.xml.data.collection.DElement;
import org.marid.spring.xml.data.collection.DValue;
import org.marid.spring.xml.data.list.DList;
import org.marid.spring.xml.data.props.DProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class ListEditor extends ListView<DElement<?>> {

    private final DCollection<?> list;
    private final Map<DElement<?>, InvalidationListener> invalidationListenerMap = new HashMap<>();

    @Autowired
    public ListEditor(DCollection<?> list) {
        super(list.elements);
        this.list = list;
    }

    @Autowired
    public void initCellFactory(Type type, IdeDependants dependants) {
        setCellFactory(param -> new TextFieldListCell<DElement<?>>() {
            @Override
            public void updateItem(DElement<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setContextMenu(null);
                    setGraphic(null);
                } else {
                    if (item instanceof DValue) {
                        setText(((DValue) item).getValue());
                        setGraphic(FontIcons.glyphIcon(FontIcon.D_COMMENT_TEXT, 16));
                    } else if (item instanceof DList) {
                        setText(s("<list>"));
                        setGraphic(FontIcons.glyphIcon(FontIcon.M_LIST, 16));
                    } else if (item instanceof DProps) {
                        setText(s("<props>"));
                        setGraphic(FontIcons.glyphIcon(FontIcon.D_VIEW_LIST, 16));
                    }
                    final Consumer<DElement<?>> consumer = e -> list.elements.set(getIndex(), e);
                    final Supplier<DElement<?>> supplier = () -> list.elements.get(getIndex());
                    setContextMenu(new ValueMenuItems(dependants, supplier, consumer, type).contextMenu());
                    item.addListener(invalidationListenerMap.compute(item, (i, old) -> {
                        if (old != null) {
                            i.removeListener(old);
                        }
                        return observable -> updateItem(i, false);
                    }));
                }
            }
        });
    }

    @PreDestroy
    public void destroy() {
        invalidationListenerMap.forEach(AbstractData::removeListener);
    }
}
