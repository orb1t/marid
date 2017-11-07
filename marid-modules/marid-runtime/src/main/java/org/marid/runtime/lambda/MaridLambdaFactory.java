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

package org.marid.runtime.lambda;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.publicLookup;

public interface MaridLambdaFactory {

	@SafeVarargs
	static <T> T lambda(Class<T> functionalInterface, Field field, AtomicStampedReference<Object>... pos) {
		try {
			return lambda(functionalInterface, publicLookup().unreflectGetter(field), pos);
		} catch (Throwable x) {
			throw new IllegalStateException(x);
		}
	}

	@SafeVarargs
	static <T> T lambda(Class<T> functionalInterface, Method method, AtomicStampedReference<Object>... pos) {
		try {
			return lambda(functionalInterface, publicLookup().unreflect(method), pos);
		} catch (Throwable x) {
			throw new IllegalStateException(x);
		}
	}

	@SafeVarargs
	static <T> T lambda(Class<T> functionalInterface, Constructor<?> method, AtomicStampedReference<Object>... pos) {
		try {
			return lambda(functionalInterface, publicLookup().unreflectConstructor(method), pos);
		} catch (Throwable x) {
			throw new IllegalStateException(x);
		}
	}

	@SafeVarargs
	static <T> T lambda(Class<T> functionalInterface, MethodHandle handle, AtomicStampedReference<Object>... pos) {
		final MethodHandle h = Stream.of(pos).reduce(
				handle,
				(a, r) -> insertArguments(a, r.getStamp(), r.getReference()),
				(h1, h2) -> h2
		);
		// TODO: use LambdaMetafactory here
		return MethodHandleProxies.asInterfaceInstance(functionalInterface, h);
	}

	static Method samMethod(Class<?> functionalInterface) {
		return Stream.of(functionalInterface.getMethods())
				.filter(m -> Modifier.isAbstract(m.getModifiers()))
				.filter(m -> !m.isDefault())
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(functionalInterface + " is not a functional"));
	}
}
