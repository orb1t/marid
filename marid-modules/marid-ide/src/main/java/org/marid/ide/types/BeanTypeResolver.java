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
import org.marid.ide.model.BeanProducerData;
import org.marid.ide.project.ProjectProfile;
import org.marid.misc.Casts;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static com.google.common.reflect.TypeToken.of;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.misc.Calls.call;
import static org.marid.runtime.beans.Bean.*;
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
            factoryToken = of(resolve(beans, classLoader, ref(beanData.getFactory())));
            factoryClass = factoryToken.getRawType();
        } else {
            factoryClass = resolveClassName(requireNonNull(type(beanData.getFactory())), classLoader);
            factoryToken = of(factoryClass).getSupertype(Casts.cast(factoryClass));
        }

        final MethodHandle returnHandle = call(() -> beanData.toBean().findProducer(factoryClass));
        final Member returnMember = call(() -> MethodHandles.reflectAs(Member.class, returnHandle));
        final Class<?> returnClass = returnHandle.type().returnType();
        final Type genericReturnType = returnMember instanceof Field
                ? ((Field) returnMember).getGenericType()
                : ((Executable) returnMember).getAnnotatedReturnType().getType();
        final TypeToken<?> genericReturnToken = of(genericReturnType).getSupertype(Casts.cast(returnClass));
        final Type returnType = factoryToken.resolveType(genericReturnToken.getType()).getType();

        final List<TypePair> pairs = new ArrayList<>();
        final BiConsumer<MethodHandle, BeanProducerData> filler = (handle, producerData) -> {
            final Member member = call(() -> MethodHandles.reflectAs(Member.class, handle));
            if (handle.type().parameterCount() != producerData.args.size()) {
                return;
            }
            final Type[] formalTypes = member instanceof Field
                    ? new Type[] {((Field) member).getGenericType()}
                    : ((Executable) member).getGenericParameterTypes();
            final Type[] actualArgTypes = new Type[formalTypes.length];
            for (int i = 0; i < actualArgTypes.length; i++) {
                final BeanMemberData beanArg = producerData.args.get(i);
                switch (beanArg.getType()) {
                    case "ref":
                        actualArgTypes[i] = resolve(beans, classLoader, beanArg.getValue());
                        break;
                    default:
                        actualArgTypes[i] = valueConverters.getType(beanArg.getType()).orElse(null);
                        break;
                }
            }
            IntStream.range(0, formalTypes.length)
                    .filter(i -> actualArgTypes[i] != null)
                    .mapToObj(i -> new TypePair(of(actualArgTypes[i]), factoryToken.resolveType(formalTypes[i])))
                    .forEach(pairs::add);
        };

        filler.accept(returnHandle, beanData.getProducer());
        final MethodHandle[] initializers = call(() -> findInitializers(returnHandle, beanData.toBean().initializers));
        for (int k = 0; k < initializers.length; k++) {
            filler.accept(initializers[k], beanData.initializers.get(k));
        }

        final TypeResolver resolver = pairs.stream().reduce(new TypeResolver(), BeanTypeResolver::resolver, (r1, r2) -> r2);

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
            return concat(of(wildcardType.getLowerBounds()), of(wildcardType.getUpperBounds()))
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
            this.actual = of(actual);
            this.formal = of(formal);
        }
    }
}
