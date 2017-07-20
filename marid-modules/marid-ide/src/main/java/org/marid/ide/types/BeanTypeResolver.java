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
import org.marid.ide.model.BeanData;
import org.marid.ide.model.BeanMemberData;
import org.marid.ide.project.ProjectProfile;
import org.marid.misc.Casts;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.marid.misc.Calls.call;
import static org.marid.runtime.beans.Bean.ref;
import static org.marid.runtime.beans.Bean.type;
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
        final IdeValueConverterManager valueConverters = new IdeValueConverterManager(classLoader);

        final Class<?> factoryClass;
        final TypeToken<?> factoryToken;
        if (ref(beanData.getFactory()) != null) {
            factoryToken = TypeToken.of(resolve(beans, classLoader, ref(beanData.getFactory())));
            factoryClass = factoryToken.getRawType();
        } else {
            factoryClass = resolveClassName(requireNonNull(type(beanData.getFactory())), classLoader);
            factoryToken = TypeToken.of(factoryClass).getSupertype(Casts.cast(factoryClass));
        }

        final MethodHandle producerHandle = call(() -> beanData.toBean().findProducer(factoryClass));
        final MethodType producerType = producerHandle.type();

        final List<TypePair> pairs = new ArrayList<>();

        final Member producerMember = call(() -> MethodHandles.reflectAs(Member.class, producerHandle));

        if (producerMember instanceof Field) {
            final Field field = (Field) producerMember;
            return factoryToken.resolveType(field.getGenericType()).getType();
        }

        if (producerType.parameterCount() == 0 || producerType.parameterCount() != beanData.getProducer().args.size()) {
            final Executable executable = (Executable) producerMember;
            final Type type = executable.getAnnotatedReturnType().getType();
            return factoryToken.resolveType(type).getType();
        }

        final Type[] actualArgTypes = new Type[producerType.parameterCount()];
        for (int i = 0; i < actualArgTypes.length; i++) {
            final BeanMemberData beanArg = beanData.getProducer().args.get(i);
            switch (beanArg.getType()) {
                case "ref":
                    actualArgTypes[i] = resolve(beans, classLoader, beanArg.getValue());
                    break;
                default:
                    actualArgTypes[i] = valueConverters.getType(beanArg.getType()).orElse(null);
                    break;
            }
        }

        final Executable executable = (Executable) producerMember;
        final Type[] formalTypes = executable.getGenericParameterTypes();

        IntStream.range(0, formalTypes.length)
                .filter(i -> actualArgTypes[i] != null)
                .mapToObj(i -> new TypePair(TypeToken.of(actualArgTypes[i]), factoryToken.resolveType(formalTypes[i])))
                .forEach(pairs::add);

        final TypeResolver resolver = pairs.stream().reduce(new TypeResolver(), BeanTypeResolver::resolver, (r1, r2) -> r2);

        final TypeToken<?> returnToken = TypeToken.of(executable.getAnnotatedReturnType().getType());
        final Class<?> returnClass = returnToken.getRawType();
        final TypeToken<?> genericReturnToken = returnToken.getSupertype(Casts.cast(returnClass));
        final Type returnType = factoryToken.resolveType(genericReturnToken.getType()).getType();

        final Type resolvedType = resolver.resolveType(returnType);
        final TypeToken<?> resolvedToken = factoryToken.resolveType(resolvedType);
        return resolvedToken.getType();
    }

    private static TypeResolver resolver(TypeResolver resolver, TypePair pair) {
        if (pair.formal.isPrimitive()) {
            return resolver;
        } else if (pair.formal.isArray()) {
            if (pair.actual.isArray()) {
                return resolver(resolver, new TypePair(pair.actual.getComponentType(), pair.formal.getComponentType()));
            } else {
                return resolver;
            }
        } else if (pair.formal.getType() instanceof TypeVariable<?>) {
            return resolver.where(pair.formal.getType(), pair.actual.getType());
        } else if (pair.formal.getType() instanceof ParameterizedType) {
            final Class<?> formalRaw = pair.formal.getRawType();
            final Class<?> actualRaw = pair.actual.getRawType();
            if (formalRaw.isAssignableFrom(actualRaw)) {
                final TypeToken<?> superType = pair.actual.getSupertype(Casts.cast(formalRaw));
                final ParameterizedType actualParameterized = (ParameterizedType) superType.getType();
                final ParameterizedType formalParameterized = (ParameterizedType) pair.formal.getType();
                final Type[] actualTypeArgs = actualParameterized.getActualTypeArguments();
                final Type[] formalTypeArgs = formalParameterized.getActualTypeArguments();
                return IntStream.range(0, actualTypeArgs.length)
                        .mapToObj(i -> new TypePair(actualTypeArgs[i], formalTypeArgs[i]))
                        .reduce(resolver, BeanTypeResolver::resolver, (r1, r2) -> r2);
            } else {
                return resolver;
            }
        } else if (pair.formal.getType() instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType) pair.formal.getType();
            return Stream.concat(Stream.of(wildcardType.getLowerBounds()), Stream.of(wildcardType.getUpperBounds()))
                    .map(t -> new TypePair(pair.actual.getType(), t))
                    .reduce(resolver, BeanTypeResolver::resolver, (r1, r2) -> r2);
        }
        return resolver;
    }

    public Type resolve(ProjectProfile profile, String beanName) {
        return resolve(profile.getBeansFile().beans, profile.getClassLoader(), beanName);
    }

    private static class TypePair {

        private final TypeToken<?> actual;
        private final TypeToken<?> formal;

        private TypePair(TypeToken<?> actual, TypeToken<?> formal) {
            this.actual = actual;
            this.formal = formal;
        }

        private TypePair(Type actual, Type formal) {
            this.actual = TypeToken.of(actual);
            this.formal = TypeToken.of(formal);
        }
    }
}
