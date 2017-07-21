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
import org.marid.ide.model.BeanMethodArgData;
import org.marid.misc.Casts;
import org.marid.runtime.exception.MaridCircularBeanException;
import org.marid.runtime.exception.MaridFilterNotFoundException;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.of;
import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTypeResolver {

    public Type resolve(BeanTypeResolverContext context, String beanName) {
        return resolveInfo(context, beanName).getType();
    }

    public BeanTypeInfo resolveInfo(BeanTypeResolverContext context, String beanName) {
        final BeanData beanData = requireNonNull(context.beanMap.get(beanName), () -> m("No such bean: {0}", beanName));
        return context.typeInfoMap.computeIfAbsent(beanName, name -> {
            if (!context.processing.add(name)) {
                throw new MaridCircularBeanException(context.processing, name);
            }
            try {
                final BeanFactoryInfo info = new BeanFactoryInfo(beanData, this, context);
                final List<TypePair> pairs = new ArrayList<>();
                final Type[] beanPs = formalTypes(info.returnHandle);
                final Type[] beanAs = info.producer.args.stream().map(a -> actualType(context, a)).toArray(Type[]::new);
                if (beanPs.length == beanAs.length) {
                    for (int i = 0; i < beanPs.length; i++) {
                        if (beanAs[i] != null) {
                            pairs.add(new TypePair(beanAs[i], info.factoryToken.resolveType(beanPs[i])));
                        }
                    }
                }
                final MethodHandle[] initializers = info.bean.findInitializers(info.returnHandle, info.bean.initializers);
                final Type[][] initPs = new Type[initializers.length][];
                final Type[][] initAs = new Type[initializers.length][];
                for (int i = 0; i < initializers.length; i++) {
                    final Type[] ps = formalTypes(initializers[i]);
                    final Type[] as = beanData.getArgs(i).map(a -> actualType(context, a)).toArray(Type[]::new);
                    if (ps.length == as.length) {
                        for (int k = 0; k < as.length; k++) {
                            if (as[k] != null) {
                                pairs.add(new TypePair(as[k], info.factoryToken.resolveType(ps[k])));
                            }
                        }
                        initPs[i] = ps;
                        initAs[i] = as;
                    } else {
                        initPs[i] = ps;
                        initAs[i] = ps;
                    }
                }

                final TypeResolver r = pairs.stream().reduce(new TypeResolver(), this::resolver, (r1, r2) -> r2);
                final Type resolvedType = r.resolveType(info.returnType);
                final TypeToken<?> resolvedToken = info.factoryToken.resolveType(resolvedType);
                final Type type = resolvedToken.getType();

                return new BeanTypeInfo(
                        type,
                        beanPs,
                        beanPs.length == beanAs.length ? beanAs : beanPs,
                        initPs,
                        initAs
                );
            } catch (RuntimeException x){
                throw x;
            } catch (Exception x) {
                throw new IllegalArgumentException(name, x);
            } finally {
                context.processing.remove(name);
            }
        });
    }

    private Type[] formalTypes(MethodHandle handle) throws IllegalAccessException {
        final Member m = MethodHandles.reflectAs(Member.class, handle);
        return m instanceof Field
                ? new Type[]{((Field) m).getGenericType()}
                : ((Executable) m).getGenericParameterTypes();
    }

    private Type actualType(BeanTypeResolverContext context, BeanMethodArgData arg) {
        final Type type;
        switch (arg.getType()) {
            case "ref":
                type = resolve(context, arg.getValue());
                break;
            default:
                type = context.converters.getType(arg.getType()).orElse(null);
                break;
        }
        if (type == null || arg.getFilter() == null) {
            return type;
        } else {
            final TypeToken<?> token = TypeToken.of(type);
            final Class<?> raw = token.getRawType();
            try {
                final Method method = raw.getMethod(arg.getFilter());
                return token.resolveType(method.getGenericReturnType()).getType();
            } catch (NoSuchMethodException | NullPointerException mx) {
                try {
                    final Field field = raw.getField(arg.getFilter());
                    return token.resolveType(field.getGenericType()).getType();
                } catch (NoSuchFieldException | NullPointerException fx) {
                    throw new MaridFilterNotFoundException(
                            arg.parent.parent.getName(),
                            arg.parent.toProducer().name(),
                            arg.getName(),
                            arg.getFilter());
                }
            }
        }
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
}
