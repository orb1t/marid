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

import org.marid.logging.LogSupport;
import org.marid.logging.Loggable;

import java.util.Arrays;
import java.util.EventObject;
import java.util.logging.Level;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProtoEvent extends EventObject implements Loggable {

    public final String message;
    public final Level level;
    public final Throwable cause;
    public final Object[] args;

    public ProtoEvent(ProtoObject source, Level level, String message, Throwable cause, Object... args) {
        super(source);
        this.message = message;
        this.cause = cause;
        this.args = args;
        this.level = level;
    }

    @Override
    public ProtoObject getSource() {
        return (ProtoObject) super.getSource();
    }

    @Override
    public void log(LogSupport logSupport) {
        logSupport.log(level, message, cause, args);
    }

    @Override
    public String toString() {
        return getSource() + " " + Arrays.asList(message, cause);
    }
}
