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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Writer;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class SubstitutedXmlEvent<D extends XMLEvent> implements XMLEvent {

    protected final D delegate;
    protected final Map<String, Object> bindings;

    public SubstitutedXmlEvent(D delegate, Map<String, Object> bindings) {
        this.delegate = delegate;
        this.bindings = bindings;
    }

    @Override
    public int getEventType() {
        return delegate.getEventType();
    }

    @Override
    public Location getLocation() {
        return delegate.getLocation();
    }

    @Override
    public boolean isStartElement() {
        return delegate.isStartElement();
    }

    @Override
    public boolean isAttribute() {
        return delegate.isAttribute();
    }

    @Override
    public boolean isNamespace() {
        return delegate.isNamespace();
    }

    @Override
    public boolean isEndElement() {
        return delegate.isEndElement();
    }

    @Override
    public boolean isEntityReference() {
        return delegate.isEntityReference();
    }

    @Override
    public boolean isProcessingInstruction() {
        return delegate.isProcessingInstruction();
    }

    @Override
    public boolean isCharacters() {
        return delegate.isCharacters();
    }

    @Override
    public boolean isStartDocument() {
        return delegate.isStartDocument();
    }

    @Override
    public boolean isEndDocument() {
        return delegate.isEndDocument();
    }

    @Override
    public StartElement asStartElement() {
        return new SubstritutedXmlStartElement(delegate.asStartElement(), bindings);
    }

    @Override
    public EndElement asEndElement() {
        return delegate.asEndElement();
    }

    @Override
    public Characters asCharacters() {
        return new SubstitutedXmlCharacters(delegate.asCharacters(), bindings);
    }

    @Override
    public QName getSchemaType() {
        return delegate.getSchemaType();
    }

    @Override
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        delegate.writeAsEncodedUnicode(writer);
    }
}
