/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class SubstritutedXmlStartElement extends SubstitutedXmlEvent<StartElement> implements StartElement {

    public SubstritutedXmlStartElement(StartElement delegate, Map<String, Object> bindings) {
        super(delegate, bindings);
    }

    @Override
    public QName getName() {
        return delegate.getName();
    }

    @Override
    public Iterator getAttributes() {
        final Iterator it = delegate.getAttributes();
        if (it != null) {
            return new Iterator() {
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Object next() {
                    Object v = it.next();
                    if (v instanceof Attribute) {
                        return new SubstitutedXmlAttribute((Attribute) v, bindings);
                    } else {
                        return v;
                    }
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        } else {
            return it;
        }
    }

    @Override
    public Iterator getNamespaces() {
        return delegate.getNamespaces();
    }

    @Override
    public Attribute getAttributeByName(QName name) {
        Attribute attribute = delegate.getAttributeByName(name);
        return attribute != null ? new SubstitutedXmlAttribute(attribute, bindings) : attribute;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return delegate.getNamespaceURI(prefix);
    }
}
