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

package org.marid.expression;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.marid.beans.IdeBean;
import org.marid.beans.TestBeanUtils;
import org.marid.io.Xmls;
import org.marid.types.GuavaTypeContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

	@Test
	void testBean1() {
		final IdeBean bean = TestBeanUtils.find(root, "b1");
		final GuavaTypeContext context = new GuavaTypeContext(bean, classLoader);
		final Type type = bean.getFactory().resolveType(null, context);
		assertEquals(String.class, type);
	}

	@Test
	void testBean2() {
		final IdeBean bean = TestBeanUtils.find(root, "b2");
		final GuavaTypeContext context = new GuavaTypeContext(bean, classLoader);
		final Type type = bean.getFactory().resolveType(null, context);
		assertEquals(BigInteger.class, type);
	}

	@Test
	void testBean3() {
		final IdeBean bean = TestBeanUtils.find(root, "b3");
		final GuavaTypeContext context = new GuavaTypeContext(bean, classLoader);
		final Type type = bean.getFactory().resolveType(null, context);
		assertEquals(new TypeToken<ArrayList<Integer>>() {}.getType(), type);
	}

	@Test
	void testBean4() {
		final IdeBean bean = TestBeanUtils.find(root, "b4");
		final GuavaTypeContext context = new GuavaTypeContext(bean, classLoader);
		final Type type = bean.getFactory().resolveType(null, context);
		assertEquals(int.class, type);
	}

	@Test
	void testBean5() {
		final IdeBean bean = TestBeanUtils.find(root, "b5");
		final GuavaTypeContext context = new GuavaTypeContext(bean, classLoader);
		final Type type = bean.getFactory().resolveType(null, context);
		assertEquals(new TypeToken<List<Long>>() {}.getType(), type);
	}
}
