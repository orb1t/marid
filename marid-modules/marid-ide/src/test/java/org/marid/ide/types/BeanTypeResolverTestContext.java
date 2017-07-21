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
import org.marid.ide.model.BeanFile;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;
import org.marid.runtime.context.MaridConfiguration;
import org.marid.runtime.context.MaridContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import static java.lang.Thread.currentThread;
import static org.marid.misc.Builder.build;

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
    public BeanData bean1() throws Exception {
        return new BeanData(
                "bean1",
                ArrayList.class.getName(),
                new BeanMethod(
                        ArrayList.class.getConstructor(Collection.class),
                        new BeanMethodArg("arg0", "ref", null, "bean2")
                )
        );
    }

    @Bean
    public BeanData bean2() throws Exception {
        return new BeanData(
                "bean2",
                Arrays.class.getName(),
                new BeanMethod(
                        Arrays.class.getMethod("asList", Object[].class),
                        new BeanMethodArg("arg0", "String[]", null, "a,b,c")
                )
        );
    }

    @Bean
    public BeanData bean3() throws Exception {
        return new BeanData(
                "bean3",
                ArrayList.class.getName(),
                new BeanMethod(ArrayList.class.getConstructor()),
                new BeanMethod(
                        ArrayList.class.getMethod("add", Object.class),
                        new BeanMethodArg("arg0", "ref", null, "bean1")
                )
        );
    }

    @Bean
    public BeanData bean4() throws Exception {
        return new BeanData(
                "bean4",
                ComplexBean.class.getName(),
                new BeanMethod(
                        ComplexBean.class.getConstructor(Set.class, Object.class),
                        new BeanMethodArg("arg0", "ref", null, "bean5"),
                        new BeanMethodArg("arg1", "Integer", null, "8")
                )
        );
    }

    @Bean
    public BeanData bean5() throws Exception {
        return new BeanData(
                "bean5",
                Collections.class.getName(),
                new BeanMethod(
                        Collections.class.getMethod("singleton", Object.class),
                        new BeanMethodArg("arg0", "String", null, "v")
                )
        );
    }

    @Bean
    public BeanData bean6() throws Exception {
        return new BeanData("bean6", "@bean4", new BeanMethod(ComplexBean.class.getField("arg")));
    }

    @Bean
    public BeanData bean7() throws Exception {
        return new BeanData(
                "bean7",
                Collections.class.getName(),
                new BeanMethod(
                        Collections.class.getMethod("singleton", Object.class),
                        new BeanMethodArg("arg0", "ref", "size", "bean5")
                )
        );
    }

    @Bean
    public BeanFile beansFile(List<BeanData> beans) {
        return build(new BeanFile(), f -> f.beans.setAll(beans));
    }

    @Bean
    public MaridContext maridContext(BeanFile beanFile) {
        return new MaridContext(new MaridConfiguration(beanFile.toBeans()));
    }

    @Bean
    public BeanTypeResolverContext beanTypeResolverContext(BeanFile beanFile) {
        return new BeanTypeResolverContext(beanFile.beans, currentThread().getContextClassLoader());
    }

    @Bean
    public Function<String, Type> typeResolver(BeanTypeResolver resolver, BeanTypeResolverContext context) {
        return name -> resolver.resolve(context, name);
    }
}
