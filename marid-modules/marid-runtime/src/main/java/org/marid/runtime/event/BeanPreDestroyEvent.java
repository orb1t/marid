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

package org.marid.runtime.event;

import org.marid.runtime.context.MaridContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class BeanPreDestroyEvent extends MaridEvent {

    @Nonnull
    private final String name;

    @Nullable
    private final Object bean;

    @Nonnull
    private final Consumer<Throwable> exceptionConsumer;

    public BeanPreDestroyEvent(@Nonnull MaridContext context,
                               @Nonnull String name,
                               @Nullable Object bean,
                               @Nonnull Consumer<Throwable> exceptionConsumer) {
        super(context);
        this.name = name;
        this.bean = bean;
        this.exceptionConsumer = exceptionConsumer;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nullable
    public Object getBean() {
        return bean;
    }

    @Nonnull
    public Consumer<Throwable> getExceptionConsumer() {
        return exceptionConsumer;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + name + ")";
    }
}
