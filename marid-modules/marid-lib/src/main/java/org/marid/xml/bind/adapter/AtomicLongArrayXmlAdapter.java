/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.xml.bind.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * @author Dmitry Ovchinnikov.
 */
public class AtomicLongArrayXmlAdapter extends XmlAdapter<String, AtomicLongArray> implements AdapterSupport {
    @Override
    public AtomicLongArray unmarshal(String v) throws Exception {
        return new AtomicLongArray(SPACE_SEPARATOR.splitAsStream(v).mapToLong(Long::parseLong).toArray());
    }

    @Override
    public String marshal(AtomicLongArray v) throws Exception {
        final StringJoiner joiner = new StringJoiner(" ");
        for (int i = 0; i < v.length(); i++) {
            joiner.add(Long.toString(v.get(i)));
        }
        return joiner.toString();
    }
}
