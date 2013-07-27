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
import javax.xml.stream.events.Attribute;

import java.util.Map;

import static org.marid.Scripting.SCRIPTING;

/**
 * @author Dmitry Ovchinnikov
 */
public class SubstitutedXmlAttribute extends SubstitutedXmlEvent<Attribute> implements Attribute {

    public SubstitutedXmlAttribute(Attribute delegate, Map<String, Object> bindings) {
        super(delegate, bindings);
    }

    @Override
    public QName getName() {
        return delegate.getName();
    }

    @Override
    public String getValue() {
        String v = delegate.getValue();
        return v == null ? null : SCRIPTING.replace(v, bindings);
    }

    @Override
    public String getDTDType() {
        return delegate.getDTDType();
    }

    @Override
    public boolean isSpecified() {
        return delegate.isSpecified();
    }
}
