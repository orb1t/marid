/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime.context;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridRuntime {

    public final Function<String, Object> beanFunc;
    public final BooleanSupplier active;
    public final ClassLoader classLoader;

    public MaridRuntime(Function<String, Object> beanFunc, BooleanSupplier active, ClassLoader classLoader) {
        this.beanFunc = beanFunc;
        this.active = active;
        this.classLoader = classLoader;
    }

    public Object getBean(String name) {
        return beanFunc.apply(name);
    }
}
