/*-
 * #%L
 * marid-fx
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

package org.marid.jfx.props;

import javafx.beans.value.WritableObjectValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class WritableValueImpl<T> implements WritableObjectValue<T> {

	private final Consumer<T> consumer;
	private final Supplier<T> supplier;

	public WritableValueImpl(Consumer<T> consumer, Supplier<T> supplier) {
		this.consumer = consumer;
		this.supplier = supplier;
	}

	@Override
	public T getValue() {
		return get();
	}

	@Override
	public void setValue(T value) {
		set(value);
	}

	@Override
	public T get() {
		return supplier.get();
	}

	@Override
	public void set(T value) {
		consumer.accept(value);
	}
}
