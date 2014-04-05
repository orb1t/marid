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

package org.marid.dyn;

import org.marid.methods.LogMethods;

import java.util.Iterator;
import java.util.logging.Logger;

import static java.lang.Thread.currentThread;
import static java.util.ServiceLoader.load;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class TypeCaster {

    private static final Logger LOG = Logger.getLogger(TypeCaster.class.getName());
    public static final TypeCaster TYPE_CASTER;

    static {
        TypeCaster typeCaster = null;
        try {
            final Iterator<TypeCaster> it = load(TypeCaster.class, currentThread().getContextClassLoader()).iterator();
            if (it.hasNext()) {
                typeCaster = it.next();
                LogMethods.fine(LOG, "Type caster {0} selected", typeCaster);
                while (it.hasNext()) {
                    LogMethods.warning(LOG, "Additional type caster {0} detected", it.next());
                }
            } else {
                typeCaster = getDefault();
                LogMethods.fine(LOG, "Default type caster has selected");
            }
        } catch (Exception x) {
            LogMethods.warning(LOG, "Type caster initialize error", x);
            typeCaster = getDefault();
        }
        if (typeCaster == null) {
            typeCaster = getDefault();
        }
        TYPE_CASTER = typeCaster;
    }

    public abstract <T> T cast(Class<T> targetType, Object value);

    public static TypeCaster getDefault() {
        return new TypeCaster() {
            @Override
            public <T> T cast(Class<T> targetType, Object value) {
                return targetType.cast(value);
            }
        };
    }
}
