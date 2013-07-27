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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.Map;

import static org.marid.Scripting.SCRIPTING;

/**
 * @author Dmitry Ovchinnikov
 */
public class SubstitutedXmlEventReader implements XMLEventReader {

    private final XMLEventReader delegate;
    private final Map<String, Object> bindings;

    public SubstitutedXmlEventReader(XMLEventReader delegate, Map<String, Object> bindings) {
        this.delegate = delegate;
        this.bindings = bindings;
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        return transform(delegate.nextEvent());
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public Object next() {
        Object v = delegate.next();
        if (v instanceof XMLEvent) {
            return transform((XMLEvent) v);
        }
        return v;
    }

    @Override
    public void remove() {
        delegate.remove();
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        return transform(delegate.peek());
    }

    @Override
    public String getElementText() throws XMLStreamException {
        String v = delegate.getElementText();
        return v == null ? null : SCRIPTING.replace(v, bindings);
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        return transform(delegate.nextTag());
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return delegate.getProperty(name);
    }

    @Override
    public void close() throws XMLStreamException {
        delegate.close();
    }

    private XMLEvent transform(final XMLEvent ev) {
        if (ev == null) {
            return null;
        } else if (ev.isAttribute()) {
            if (ev instanceof Attribute) {
                return new SubstitutedXmlAttribute((Attribute) ev, bindings);
            } else {
                return ev;
            }
        } else if (ev.isStartElement()) {
            return new SubstritutedXmlStartElement(ev.asStartElement(), bindings);
        } else if (ev.isCharacters()) {
            return new SubstitutedXmlCharacters(ev.asCharacters(), bindings);
        } else {
            return ev;
        }
    }
}
