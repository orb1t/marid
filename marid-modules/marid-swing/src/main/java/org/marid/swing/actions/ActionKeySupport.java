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

package org.marid.swing.actions;

import org.marid.dyn.MetaInfo;
import org.marid.logging.LogSupport;
import org.marid.reflect.ReflectionUtils;

import javax.swing.*;
import java.lang.reflect.Field;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ActionKeySupport extends LogSupport {

    default ActionMap actions() {
        final ActionMap actionMap = new ActionMap();
        for (final Field field : ReflectionUtils.getFields(getClass())) {
            if (!field.isAnnotationPresent(MetaInfo.class) || !Action.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                final Action action = (Action) field.get(this);
                final MetaInfo metaInfo = field.getAnnotation(MetaInfo.class);
                actionMap.put(new ActionKey(metaInfo.path()), action);
            } catch (ReflectiveOperationException x) {
                log(WARNING, "Unable to get action from {0}", x, field);
            }
        }
        return actionMap;
    }
}
