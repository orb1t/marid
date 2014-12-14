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

package org.marid.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov.
 */
public class Builder<T> {

    protected final T target;

    public Builder(T target) {
        this.target = target;
    }

    public <P> Builder<T> with(BiConsumer<T, P> consumer, P value) {
        consumer.accept(target, value);
        return this;
    }

    public Builder<T> apply(Consumer<T> consumer) {
        consumer.accept(target);
        return this;
    }

    public <P> Builder<T> $(BiConsumer<T, P> consumer, P value) {
        return with(consumer, value);
    }

    public Builder<T> $(Consumer<T> consumer) {
        return apply(consumer);
    }

    public T build() {
        return target;
    }
}
