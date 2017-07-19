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

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import org.marid.ide.model.BeanData;
import org.marid.ide.model.BeanMemberData;
import org.marid.ide.project.ProjectProfile;
import org.marid.misc.Calls;
import org.marid.runtime.beans.BeanFactory;
import org.marid.runtime.beans.MaridFactoryBean;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.List;

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
            token = TypeToken.of(factoryClass).constructor(c).getReturnType();
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
            final com.fasterxml.classmate.TypeResolver res = new com.fasterxml.classmate.TypeResolver();
            final MemberResolver r = new MemberResolver(res);
            TypeResolver resolver = new TypeResolver();
            for (int i = 0; i < args.length; i++) {
                final BeanMemberData beanArg = beanData.args.get(i);
                final Type type;
                switch (beanArg.getType()) {
                    case "ref":
                        type = resolve(beans, classLoader, beanArg.getValue());
                        break;
                    default:
                        type = valueConverters.getType(beanArg.getType());
                        break;
                }
                if (type != null) {
                    //resolver = resolver.where(args[i], type);
                }
            }
            final ResolvedTypeWithMembers members = r.resolve(res.resolve(token.getType()), null, null);

            return resolver.resolveType(token.getType());
        } else {
            return token.getType();
        }
    }

    public Type resolve(ProjectProfile profile, String beanName) {
        return resolve(profile.getBeansFile().beans, profile.getClassLoader(), beanName);
    }
}
