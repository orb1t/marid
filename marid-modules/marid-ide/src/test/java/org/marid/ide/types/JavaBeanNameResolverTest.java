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

package org.marid.ide.types;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static com.github.javaparser.JavaParser.parseType;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
@ContextConfiguration(classes = {JavaBeanNameResolverTestContext.class})
public class JavaBeanNameResolverTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private JavaBeanNameResolver resolver;

    @Autowired
    private Path javaFileA;

    @Test
    public void testLongLong() {
        final Set<String> names = resolver.beanNames(javaFileA, parseType("java.util.function.Function<Long, Long>"));
        assertEquals(Collections.singleton("bean2"), names);
    }

    @Test
    public void testIntegerLong() {
        final Set<String> names = resolver.beanNames(javaFileA, parseType("java.util.function.Function<Integer, Long>"));
        assertEquals(Collections.singleton("bean1"), names);
    }
}
