/*-
 * #%L
 * marid-util
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

package org.marid.misc;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 */
public class Builder<T> {

	final T builder;

	public Builder(T builder) {
		this.builder = builder;
	}

	public <A> Builder<T> set(BiConsumer<T, A> consumer, A arg) {
		consumer.accept(builder, arg);
		return this;
	}

	public T build() {
		return builder;
	}

	public static <T> T build(T arg, Consumer<T> consumer) {
		consumer.accept(arg);
		return arg;
	}

	public static <T> T getFrom(Supplier<T> supplier, Supplier<T> newObjectSupplier, Consumer<T> consumer) {
		final T oldValue = supplier.get();
		if (oldValue == null) {
			final T newValue = newObjectSupplier.get();
			consumer.accept(newValue);
			return newValue;
		} else {
			return oldValue;
		}
	}
}
