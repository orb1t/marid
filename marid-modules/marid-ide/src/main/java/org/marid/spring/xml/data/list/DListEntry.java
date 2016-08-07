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

package org.marid.spring.xml.data.list;

import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.ValueHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov.
 */
public class DListEntry extends ValueHolder<DListEntry> {

    @Override
    public Optional<? extends Type> getType(ProjectProfile profile) {
        return Optional.empty();
    }

    @Override
    public void save(Node node, Document document) {
        if (isEmpty()) {
            return;
        }
        save((Element) node, document);
    }

    @Override
    public void load(Node node, Document document) {
        final Element element = (Element) node;
        switch (element.getNodeName()) {
            case "value":

                break;
        }
    }
}
