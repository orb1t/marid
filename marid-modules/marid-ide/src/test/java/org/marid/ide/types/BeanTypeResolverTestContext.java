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

import org.marid.ide.model.BeanData;
import org.marid.runtime.beans.B;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;
import org.marid.runtime.context.MaridContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import static java.lang.Thread.currentThread;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class BeanTypeResolverTestContext {

    @Bean
    public BeanTypeResolver beanTypeResolver() {
        return new BeanTypeResolver();
    }

    @Bean
    public BeanData root() {
        return new BeanData();
    }

    @Bean
    public BeanData bean1(BeanData root) throws Exception {
        return root.add(new B("bean1",
                ArrayList.class.getName(),
                ArrayList.class.getConstructor(Collection.class),
                new BeanMethodArg("arg0", "ref", "bean2")));
    }

    @Bean
    public BeanData bean2(BeanData root) throws Exception {
        return root.add(new B("bean2",
                Arrays.class.getName(),
                Arrays.class.getMethod("asList", Object[].class),
                new BeanMethodArg("arg0", "String[]", "a,b,c")));
    }

    @Bean
    public BeanData bean3(BeanData root) throws Exception {
        return root.add(new B("bean3", ArrayList.class.getName(), ArrayList.class.getConstructor())
                .add(new BeanMethod(
                        ArrayList.class.getMethod("add", Object.class),
                        new BeanMethodArg("arg0", "ref", "bean1")
                ))
        );
    }

    @Bean
    public BeanData bean4(BeanData root) throws Exception {
        return root.add(new B("bean4",
                ComplexBean.class.getName(),
                ComplexBean.class.getConstructor(Set.class, Object.class),
                new BeanMethodArg("arg0", "ref", "bean5"),
                new BeanMethodArg("arg1", "Integer", "8")));
    }

    @Bean
    public BeanData bean5(BeanData root) throws Exception {
        return root.add(new B("bean5",
                Collections.class.getName(),
                Collections.class.getMethod("singleton", Object.class),
                new BeanMethodArg("arg0", "String", "v")));
    }

    @Bean
    public BeanData bean6(BeanData root) throws Exception {
        return root.add(new B("bean6", "bean4", ComplexBean.class.getField("arg")));
    }

    @Bean
    public BeanData bean7(BeanData root) throws Exception {
        return root.add(new B("bean7",
                Collections.class.getName(),
                Collections.class.getMethod("singleton", Object.class),
                new BeanMethodArg("arg0", "Integer", "1")));
    }

    @Bean
    public BeanData bean8(BeanData root) throws Exception {
        return root.add(new B("bean8",
                AnotherComplexBean.class.getName(),
                AnotherComplexBean.class.getConstructor(Object.class, Object.class),
                new BeanMethodArg("arg0", "Integer", "1"),
                new BeanMethodArg("arg1", "Long", "2")));
    }

    @Bean
    public BeanData bean9(BeanData root) throws Exception {
        return root.add(new B("bean9",
                ComplexBean.class.getName(),
                ComplexBean.class.getConstructor(Set.class, Object.class),
                new BeanMethodArg("arg0", "of", null),
                new BeanMethodArg("arg1", "Integer", "1")));
    }

    @Bean
    public MaridContext maridContext(BeanData root) {
        return new MaridContext(root.toBean());
    }

    @Bean
    public BeanContext beanTypeResolverContext(BeanData root) {
        return new BeanContext(root, currentThread().getContextClassLoader());
    }
}
