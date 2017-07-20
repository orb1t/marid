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
import org.marid.misc.Casts;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.context.CircularBeanReferenceException;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.reflect.TypeToken.of;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.of;
import static org.marid.l10n.L10n.m;
import static org.marid.runtime.beans.Bean.*;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTypeResolver {

    public Type resolve(BeanTypeResolverContext context, String beanName) {
        final BeanData beanData = requireNonNull(context.beanMap.get(beanName), () -> m("No such bean: {0}", beanName));
        return context.resolved.computeIfAbsent(beanName, name -> {
            if (!context.processing.add(name)) {
                throw new CircularBeanReferenceException(context.processing, name);
            }
            final Bean bean = beanData.toBean();
            try {
                final String factory = requireNonNull(beanData.getFactory(), () -> m("Factory is null: {0}", name));
                final Class<?> factoryClass;
                final TypeToken<?> factoryToken;
                if (ref(factory) != null) {
                    factoryToken = of(resolve(context, ref(factory)));
                    factoryClass = factoryToken.getRawType();
                } else {
                    final String className = requireNonNull(type(factory), () -> m("Factory class is null: {0}", name));
                    factoryClass = resolveClassName(className, context.getClassLoader());
                    factoryToken = of(factoryClass).getSupertype(Casts.cast(factoryClass));
                }
                final MethodHandle returnHandle = bean.findProducer(factoryClass);
                final Member returnMember = MethodHandles.reflectAs(Member.class, returnHandle);
                final Class<?> returnClass = returnHandle.type().returnType();
                final Type genericReturnType = returnMember instanceof Field
                        ? ((Field) returnMember).getGenericType()
                        : ((Executable) returnMember).getAnnotatedReturnType().getType();
                final TypeToken<?> genericReturnToken = genericReturnType instanceof Class<?>
                        ? of(genericReturnType).getSupertype(Casts.cast(returnClass))
                        : of(genericReturnType);
                final Type returnType = factoryToken.resolveType(genericReturnToken.getType()).getType();

                final List<TypePair> pairs = new ArrayList<>();
                context.fill(this, pairs, returnHandle, beanData.getProducer(), factoryToken);
                final MethodHandle[] initializers = findInitializers(returnHandle, bean.initializers);
                for (int k = 0; k < initializers.length; k++) {
                    context.fill(this, pairs, initializers[k], beanData.initializers.get(k), factoryToken);
                }

                final TypeResolver resolver = pairs.stream().reduce(new TypeResolver(), this::resolver, (r1, r2) -> r2);
                final Type resolvedType = resolver.resolveType(returnType);
                final TypeToken<?> resolvedToken = factoryToken.resolveType(resolvedType);
                return resolvedToken.getType();
            } catch (Exception x) {
                throw new IllegalArgumentException(name, x);
            } finally {
                context.processing.remove(name);
            }
        });
    }

    private TypeResolver resolver(TypeResolver resolver, TypePair pair) {
        if (pair.formal.isPrimitive()) {
            return resolver;
        } else if (pair.formal.isArray()) {
            if (pair.actual.isArray()) {
                return resolver(resolver, new TypePair(pair.actual.getComponentType(), pair.formal.getComponentType()));
            } else {
                return resolver;
            }
        } else if (pair.formal.getType() instanceof TypeVariable<?>) {
            final TypeVariable<?> typeVariable = (TypeVariable<?>) pair.formal.getType();
            return Stream.of(typeVariable.getBounds())
                    .map(t -> new TypePair(pair.actual.getType(), t))
                    .reduce(resolver, this::resolver, (r1, r2) -> r2)
                    .where(pair.formal.getType(), pair.actual.getType());
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
                        .reduce(resolver, this::resolver, (r1, r2) -> r2);
            } else {
                return resolver;
            }
        } else if (pair.formal.getType() instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType) pair.formal.getType();
            return of(wildcardType.getUpperBounds())
                    .map(t -> new TypePair(pair.actual.getType(), t))
                    .reduce(resolver, this::resolver, (r1, r2) -> r2);
        } else {
            return resolver;
        }
    }

    static class TypePair {

        private final TypeToken<?> actual;
        private final TypeToken<?> formal;

        private TypePair(TypeToken<?> actual, TypeToken<?> formal) {
            this.actual = actual;
            this.formal = formal;
        }

        private TypePair(Type actual, Type formal) {
            this(of(actual), of(formal));
        }

        TypePair(Type actual, TypeToken<?> formal) {
            this(of(actual), formal);
        }
    }
}
