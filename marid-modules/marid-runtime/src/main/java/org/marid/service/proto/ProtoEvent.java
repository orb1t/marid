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

package org.marid.service.proto;

import java.util.Arrays;
import java.util.EventObject;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProtoEvent extends EventObject {

    private final String type;
    private final Throwable cause;

    public ProtoEvent(ProtoObject source, String type, Throwable cause) {
        super(source);
        this.type = type;
        this.cause = cause;
    }

    @Override
    public ProtoObject getSource() {
        return (ProtoObject) super.getSource();
    }

    public String getType() {
        return type;
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return getSource() + " " + Arrays.asList(type, cause);
    }
}
