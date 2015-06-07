/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.bd.blocks.proto;

import org.codehaus.groovy.ast.expr.MapExpression;
import org.marid.xml.bind.adapter.MapExpressionXmlAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement
public final class PbBusDescriptor {

    @XmlAttribute
    public final String name;

    @XmlElement
    @XmlJavaTypeAdapter(MapExpressionXmlAdapter.class)
    public final MapExpression map;

    public PbBusDescriptor(String name, MapExpression map) {
        this.name = name;
        this.map = map;
    }

    public PbBusDescriptor() {
        this(null, null);
    }
}
