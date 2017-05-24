/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.dependant.beantree.items;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import org.marid.dependant.beantree.data.ItemGraphicFactory;
import org.marid.dependant.beantree.data.ItemTextFactory;
import org.marid.spring.xml.DCollection;
import org.marid.spring.xml.DElement;
import org.marid.spring.xml.DElementHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PostConstruct;
import java.util.function.Function;

import static org.marid.jfx.beans.ConstantValue.bind;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class DataTreeItem<T extends DElementHolder> extends AbstractTreeItem<T> {

    public DataTreeItem(T elem) {
        super(elem);
    }

    @Override
    public ObservableValue<String> getName() {
        return elem.nameProperty();
    }

    @PostConstruct
    private void init() {
        bind(graphic, () -> ItemGraphicFactory.graphic(elem.dataProperty()));
        bind(text, () -> ItemTextFactory.text(elem.dataProperty()));
    }

    @Autowired
    private void initChildren(GenericApplicationContext context) {
        final ListChangeListener<DElement> listChangeListener = c -> {

        };
        final ChangeListener<DElement> listener = (observable, oldElement, newElement) -> {
            if (oldElement == newElement) {
                return;
            }
            if (oldElement instanceof DCollection) {
                ((DCollection) oldElement).elements.removeListener(listChangeListener);
            }
            if (newElement instanceof DCollection) {
                final DCollection collection = (DCollection) newElement;
                final Function<DElement, DElementHolder> holderFunction = e -> {
                    return null;
                };
                getChildren().removeIf(item -> {
                    context.getBeanFactory().destroyBean(item);
                    return true;
                });

            }
        };
        elem.dataProperty().addListener(listener);
        destroyActions.add(() -> elem.dataProperty().removeListener(listener));
    }
}
