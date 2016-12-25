/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.beans.testbeans;

import org.marid.beans.BeanEditor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class Bean1Editor implements BeanEditor<Bean1> {
    @Nonnull
    @Override
    public Set<Class<? extends Bean1>> editors(@Nonnull Class<Bean1> type) {
        return Collections.singleton(Bean1Descriptor.class);
    }
}
