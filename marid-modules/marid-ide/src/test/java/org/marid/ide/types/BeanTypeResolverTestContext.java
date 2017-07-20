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
import org.marid.ide.model.BeanMemberData;
import org.marid.ide.model.BeanProducerData;
import org.marid.ide.model.BeansFile;
import org.marid.runtime.context.MaridConfiguration;
import org.marid.runtime.context.MaridContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import static java.lang.Thread.currentThread;
import static org.marid.misc.Builder.build;
import static org.marid.misc.Calls.call;
import static org.marid.runtime.beans.Bean.signature;

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
    public BeanData bean1() {
        return build(new BeanData(), b -> {
            b.name.set("bean1");
            b.factory.set(ArrayList.class.getName());
            b.producer.set(build(new BeanProducerData(), p -> {
                p.signature.set(call(() -> signature(ArrayList.class.getConstructor(Collection.class))));
                p.args.add(build(new BeanMemberData(), a -> {
                    a.name.set("arg0");
                    a.type.set("ref");
                    a.value.set("bean2");
                }));
            }));
        });
    }

    @Bean
    public BeanData bean2() {
        return build(new BeanData(), b -> {
            b.name.set("bean2");
            b.factory.set(Arrays.class.getName());
            b.producer.set(build(new BeanProducerData(), p -> {
                p.signature.set(call(() -> signature(Arrays.class.getMethod("asList", Object[].class))));
                p.args.add(build(new BeanMemberData(), a -> {
                    a.name.set("arg0");
                    a.type.set("String[]");
                    a.value.set("a,b,c");
                }));
            }));
        });
    }

    @Bean
    public BeanData bean3() {
        return build(new BeanData(), b -> {
            b.name.set("bean3");
            b.factory.set(ArrayList.class.getName());
            b.producer.set(build(new BeanProducerData(), p -> {
                p.signature.set(call(() -> signature(ArrayList.class.getConstructor())));
            }));
            b.initializers.add(build(new BeanProducerData(), p -> {
                p.signature.set(call(() -> signature(ArrayList.class.getMethod("add", Object.class))));
                p.args.add(build(new BeanMemberData(), a -> {
                    a.name.set("arg0");
                    a.type.set("ref");
                    a.value.set("bean1");
                }));
            }));
        });
    }

    @Bean
    public BeanData bean4() {
        return build(new BeanData(), b -> {
            b.name.set("bean4");
            b.factory.set(ComplexBean.class.getName());
            b.producer.set(build(new BeanProducerData(), p -> {
                p.signature.set(call(() -> signature(ComplexBean.class.getConstructor(java.util.Set.class, java.lang.Object.class))));
                p.args.add(build(new BeanMemberData(), a -> {
                    a.name.set("arg0");
                    a.type.set("ref");
                    a.value.set("bean5");
                }));
                p.args.add(build(new BeanMemberData(), a -> {
                    a.name.set("arg1");
                    a.type.set("Integer");
                    a.value.set("8");
                }));
            }));
        });
    }

    @Bean
    public BeanData bean5() {
        return build(new BeanData(), b -> {
            b.name.set("bean5");
            b.factory.set(Collections.class.getName());
            b.producer.set(build(new BeanProducerData(), p -> {
                p.signature.set(call(() -> signature(Collections.class.getMethod("singleton", Object.class))));
                p.args.add(build(new BeanMemberData(), a -> {
                    a.name.set("arg0");
                    a.type.set("String");
                    a.value.set("v");
                }));
            }));
        });
    }

    @Bean
    public BeanData bean6() {
        return build(new BeanData(), b -> {
            b.name.set("bean6");
            b.factory.set("@bean4");
            b.producer.set(build(new BeanProducerData(), p -> {
                p.signature.set(call(() -> signature(ComplexBean.class.getField("arg"))));
            }));
        });
    }

    @Bean
    public BeansFile beansFile(List<BeanData> beans) {
        return build(new BeansFile(), f -> f.beans.setAll(beans));
    }

    @Bean
    public MaridContext maridContext(BeansFile beansFile) {
        return new MaridContext(new MaridConfiguration(beansFile.toBeans()));
    }

    @Bean
    public Function<String, Type> typeResolver(BeanTypeResolver resolver, BeansFile file) {
        return name -> resolver.resolve(file.beans, currentThread().getContextClassLoader(), name);
    }
}
