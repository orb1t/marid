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

package org.marid.service.proto.model;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ProtoTaskSupport {

    static void putProperties(Map<String, Object> map, AbstractProtoObject object) {
        object.putProperty(map, "interruptTask", boolean.class);
        object.putProperty(map, "delayMode", boolean.class);
    }

    default boolean isInterruptTask() {
        return ((AbstractProtoObject) this).getProperty("interruptTask", () -> true);
    }

    default boolean isDelayMode() {
        return ((AbstractProtoObject) this).getProperty("delayMode", () -> true);
    }
}
