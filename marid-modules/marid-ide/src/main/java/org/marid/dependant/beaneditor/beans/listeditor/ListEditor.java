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

import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import org.marid.spring.xml.data.collection.DCollection;
import org.marid.spring.xml.data.collection.DElement;
import org.marid.spring.xml.data.collection.DValue;
import org.marid.spring.xml.data.list.DList;
import org.marid.spring.xml.data.props.DProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class ListEditor extends ListView<DElement<?>> {

    private final DCollection<?> list;

    @Autowired
    public ListEditor(DCollection<?> list) {
        super(list.elements);
        this.list = list;
        setCellFactory(param -> new TextFieldListCell<DElement<?>>() {
            @Override
            public void updateItem(DElement<?> item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    if (item instanceof DValue) {
                        setText(((DValue) item).getValue());
                    } else if (item instanceof DList) {
                        setText(s("<list>"));
                    } else if (item instanceof DProps) {
                        setText(s("<props>"));
                    }
                }
            }
        });
    }
}
