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

package org.marid.service.util;

import org.marid.dyn.Casting;
import org.marid.util.Utils;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * @author Dmitry Ovchinnikov
 */
public class MapUtil {

    public static String name(Object name) {
        return name == null ? UUID.randomUUID().toString() : Casting.castTo(String.class, name);
    }

    public static Map<Object, Map<String, Object>> children(Map<String, Object> map, String key) {
        return Utils.cast(map.getOrDefault(key, Collections.emptyMap()));
    }

    public static Map<String, Object> variables(Map<String, Object> map) {
        return Utils.cast(map.getOrDefault("variables", Collections.emptyMap()));
    }
}
