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

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import javafx.util.Pair;
import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;
import org.marid.misc.Calls;
import org.marid.runtime.beans.BeanFactory;
import org.marid.runtime.beans.MaridFactoryBean;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTypeResolver {

    public Type resolve(List<BeanData> beans, ClassLoader classLoader, String beanName) {
        final BeanData beanData = beans.stream()
                .filter(e -> beanName.equals(e.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(beanName));
        final MaridFactoryBean factoryBean = new MaridFactoryBean(beanData.getProducer());
        final BeanFactory beanFactory = new BeanFactory(beanData.getFactory());
        final IdeValueConverterManager valueConverters = new IdeValueConverterManager(classLoader);

        final TypeToken<?> factoryToken;
        final Class<?> factoryClass;
        if (beanFactory.ref != null) {
            factoryToken = TypeToken.of(resolve(beans, classLoader, beanFactory.ref));
            factoryClass = factoryToken.getRawType();
        } else {
            factoryClass = resolveClassName(requireNonNull(beanFactory.factoryClass), classLoader);
            factoryToken = TypeToken.of(factoryClass);
        }

        final MethodHandle producerHandle = Calls.call(() -> factoryBean.findProducer(factoryClass));
        final Member producerMember = Calls.call(() -> MethodHandles.reflectAs(Member.class, producerHandle));
        final Type[] args;
        final TypeToken<?> token;
        if (producerMember instanceof Constructor<?>) {
            final Constructor<?> c = (Constructor<?>) producerMember;
            token = TypeToken.of(factoryClass);
            args = c.getGenericParameterTypes();
        } else if (producerMember instanceof Method) {
            final Method m = (Method) producerMember;
            token = factoryToken.resolveType(m.getGenericReturnType());
            args = m.getGenericParameterTypes();
        } else {
            final Field f = (Field) producerMember;
            token = TypeToken.of(f.getGenericType());
            args = new Type[0];
        }
        if (args.length == beanData.args.size()) {
            final TypeResolver typeResolver = IntStream.range(0, args.length)
                    .mapToObj(i -> new Pair<>(args[i], valueConverters.getType(beanData.args.get(i).getType())))
                    .reduce(new TypeResolver(), (a, e) -> a.where(e.getKey(), e.getValue()), (v1, v2) -> v2);
            return typeResolver.resolveType(token.getType());
        } else {
            return token.getType();
        }
    }

    public Type resolve(ProjectProfile profile, String beanName) {
        return resolve(profile.getBeansFile().beans, profile.getClassLoader(), beanName);
    }
}
