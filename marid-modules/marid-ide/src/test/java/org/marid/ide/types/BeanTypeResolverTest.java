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
import org.marid.ide.model.BeanData;
import org.marid.test.NormalTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.List;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
@ContextConfiguration(classes = {BeanTypeResolverTestContext.class})
public class BeanTypeResolverTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private BeanTypeResolver resolver;

    @Autowired
    private List<BeanData> beans;

    @Autowired
    private BeanContext context;

    @Test
    public void allBeans() {
        for (final BeanData bean : beans) {
            final BeanTypeInfo type = resolver.resolve(context, bean);
            log(INFO, "{0}: {1}", bean.getName(), type);
        }
    }

    @Test
    public void testBean1() {
    }
}
