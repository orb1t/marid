/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.expression.mutable;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.idelib.beans.BeanUtils;
import org.marid.idelib.beans.IdeBean;
import org.marid.io.Xmls;
import org.marid.types.GuavaTypeContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionTypeTest {

	private static ClassLoader classLoader;
	private static IdeBean root;

	@BeforeAll
	static void init() throws IOException {
		classLoader = Thread.currentThread().getContextClassLoader();
		try (final Reader reader = new InputStreamReader(classLoader.getResourceAsStream("tbeans1.xml"), UTF_8)) {
			root = Xmls.read(reader, e -> new IdeBean(null, e));
		}
	}

	private static Stream<Arguments> testData() {
		return Stream.of(
				() -> new Object[] {"b1", String.class},
				() -> new Object[] {"b2", BigInteger.class},
				() -> new Object[] {"b3", new TypeToken<ArrayList<Integer>>() {}.getType()},
				() -> new Object[] {"b4", int.class},
				() -> new Object[] {"b5", new TypeToken<List<Long>>() {}.getType()},
				() -> new Object[] {"b6", new TypeToken<List<List<Long>>>() {}.getType()},
				() -> new Object[] {"b7", new TypeToken<List<Integer>>() {}.getType()},
				() -> new Object[] {"b8", new TypeToken<ArrayList<Number>>() {}.getType()},
				() -> new Object[] {"b9", new TypeToken<List<List<Integer>>>() {}.getType()},
				() -> new Object[] {"bA", new TypeToken<List<Long>[]>() {}.getType()},
				() -> new Object[] {"bB", int[].class}
		);
	}

	@ParameterizedTest
	@MethodSource("testData")
	void testBean(String beanName, Type expectedType) {
		final IdeBean bean = BeanUtils.find(root, beanName);
		final GuavaTypeContext context = new GuavaTypeContext(bean, classLoader);
		final Type type = bean.getFactory().getType(null, context);
		assertEquals(expectedType, type);
	}
}
