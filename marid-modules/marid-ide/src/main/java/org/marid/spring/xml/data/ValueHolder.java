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

package org.marid.spring.xml.data;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.list.DList;
import org.marid.spring.xml.data.props.DProps;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class ValueHolder<T extends ValueHolder<T>> extends AbstractData<T> {

    public final StringProperty value = new SimpleStringProperty(this, "value");
    public final ObjectProperty<DProps> props = new SimpleObjectProperty<>(this, "props");
    public final ObjectProperty<DList> list = new SimpleObjectProperty<>(this, "list");

    @Override
    public void save(Node node, Document document) {
        if (props.isNotNull().get()) {
            props.get().save(node, document);
        } else if (list.isNotNull().get()) {
            list.get().save(node, document);
        }
    }

    @Override
    public void load(Node node, Document document) {
        switch (node.getNodeName()) {
            case "value":
                value.set(node.getTextContent());
                break;
            case "props":
                final DProps props = new DProps();
                props.load(node, document);
                this.props.set(props);
                break;
            case "list":
                final DList list = new DList();
                list.load(node, document);
                this.list.set(list);
                break;
        }
    }

    public boolean isEmpty() {
        return Stream.of(props, list)
                .allMatch(e -> e.isNull().get());
    }

    public abstract Optional<? extends Type> getType(ProjectProfile profile);
}
