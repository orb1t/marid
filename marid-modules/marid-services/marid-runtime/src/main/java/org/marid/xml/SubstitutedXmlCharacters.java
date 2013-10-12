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

import org.marid.groovy.GroovyRuntime;

import javax.xml.stream.events.Characters;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class SubstitutedXmlCharacters extends SubstitutedXmlEvent<Characters> implements Characters {

    public SubstitutedXmlCharacters(Characters delegate, Map<String, Object> bindings) {
        super(delegate, bindings);
    }

    @Override
    public String getData() {
        String v = delegate.getData();
        return v == null ? v : GroovyRuntime.replace(v, bindings);
    }

    @Override
    public boolean isWhiteSpace() {
        return delegate.isWhiteSpace();
    }

    @Override
    public boolean isCData() {
        return delegate.isCData();
    }

    @Override
    public boolean isIgnorableWhiteSpace() {
        return delegate.isIgnorableWhiteSpace();
    }
}
