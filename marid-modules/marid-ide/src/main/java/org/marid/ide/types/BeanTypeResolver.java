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
import org.marid.runtime.exception.MaridFilterNotFoundException;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.google.common.reflect.TypeToken.of;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTypeResolver {

    public BeanTypeInfo resolve(BeanContext context, String beanName) {
        final BeanData beanData = context.getBean(beanName);
        return context.typeInfoMap.computeIfAbsent(beanName, name -> context.process(name, () -> {
            try {
                final BeanFactoryInfo info = new BeanFactoryInfo(beanData, this, context);
                final Map<TypeToken<?>, List<TypeToken<?>>> pairs = new LinkedHashMap<>();
                final Type[] beanPs = formalTypes(info.returnHandle, true);
                final Type[] beanAs = new Type[beanPs.length];
                for (int k = 0; k < beanAs.length; k++) {
                    beanAs[k] = actualType(context, info.producer.args.get(k), beanPs[k]);
                }
                for (int i = 0; i < beanPs.length; i++) {
                    if (beanAs[i] != null) {
                        resolve(pairs, info.factoryToken.resolveType(beanPs[i]), of(beanAs[i]));
                    }
                }
                final Type[][] initPs = new Type[info.bean.initializers.length][];
                final Type[][] initAs = new Type[info.bean.initializers.length][];
                for (int i = 0; i < info.bean.initializers.length; i++) {
                    final MethodHandle handle = info.bean.findInitializer(info.returnHandle, info.bean.initializers[i]);
                    final Type[] ps = formalTypes(handle, false);
                    final Type[] as = new Type[ps.length];
                    for (int k = 0; k < ps.length; k++) {
                        as[k] = actualType(context, beanData.getArgs(i).get(k), ps[k]);
                    }
                    for (int k = 0; k < as.length; k++) {
                        if (as[k] != null) {
                            resolve(pairs, info.factoryToken.resolveType(ps[k]), of(as[k]));
                        }
                    }
                    initPs[i] = ps;
                    initAs[i] = as;
                }

                final TypeResolver r = pairs.entrySet().stream().reduce(
                        new TypeResolver(),
                        (a, e) -> a.where(e.getKey().getType(), commonAncestor(e).getType()),
                        (r1, r2) -> r2
                );
                final UnaryOperator<Type> resolverFunc = t -> info.factoryToken.resolveType(r.resolveType(t)).getType();
                return new BeanTypeInfo(resolverFunc, info.returnType, beanPs, beanPs, initPs, initAs);
            } catch (RuntimeException x) {
                throw x;
            } catch (Exception x) {
                throw new IllegalArgumentException(name, x);
            }
        }));
    }

    private TypeToken<?> commonAncestor(Entry<TypeToken<?>, List<TypeToken<?>>> entry) {
        final List<TypeToken<?>> tokens = entry.getValue();
        if (tokens.size() == 1 || tokens.stream().allMatch(t -> t.equals(tokens.get(0)))) {
            return tokens.get(0);
        } else {
            final TypeToken<?>[][] sets = tokens.stream()
                    .map(s -> s.getTypes().toArray(new TypeToken<?>[0]))
                    .toArray(TypeToken<?>[][]::new);
            final int max = Stream.of(sets).mapToInt(s -> s.length).max().orElse(0);
            for (int i = 0; i < max; i++) {
                for (final TypeToken<?>[] set : sets) {
                    if (i < set.length) {
                        final TypeToken<?> candidate = set[i];
                        if (tokens.stream().allMatch(t -> t.isSubtypeOf(candidate))) {
                            return candidate;
                        }
                    }
                }
            }
            return entry.getKey();
        }
    }

    private Type[] formalTypes(MethodHandle handle, boolean getters) throws IllegalAccessException {
        final Member m = MethodHandles.reflectAs(Member.class, handle);
        return m instanceof Field
                ? (getters ? new Type[0] : new Type[]{((Field) m).getGenericType()})
                : ((Executable) m).getGenericParameterTypes();
    }

    private Type actualType(BeanContext context, BeanMethodArgData arg, Type formalType) {
        final Type type;
        switch (arg.getType()) {
            case "ref":
                type = resolve(context, arg.getValue()).getType();
                break;
            default: {
                final Type t = context.getConverters().getType(arg.getType()).orElse(null);
                type = t instanceof WildcardType ? formalType : t;
            }
        }
        if (type == null || arg.getFilter() == null) {
            return type;
        } else {
            final TypeToken<?> token = of(type);
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
                            arg.parent.toMethod().name(),
                            arg.getName(),
                            arg.getFilter());
                }
            }
        }
    }

    private void resolve(Map<TypeToken<?>, List<TypeToken<?>>> map, TypeToken<?> formal, TypeToken<?> actual) {
        resolve(new HashSet<>(), map, formal, actual);
    }

    private void resolve(Set<TypeToken<?>> passed, Map<TypeToken<?>, List<TypeToken<?>>> map, TypeToken<?> formal, TypeToken<?> actual) {
        if (!passed.add(formal)) {
            return;
        }
        if (formal.isArray() && actual.isArray()) {
            resolve(passed, map, formal.getComponentType(), actual.getComponentType());
        } else if (formal.getType() instanceof TypeVariable<?>) {
            final TypeVariable<?> typeVariable = (TypeVariable<?>) formal.getType();
            for (final Type bound : typeVariable.getBounds()) {
                resolve(passed, map, of(bound), actual);
            }
            map.computeIfAbsent(formal, k -> new ArrayList<>()).add(actual.wrap());
        } else if (formal.getType() instanceof ParameterizedType) {
            final Class<?> formalRaw = formal.getRawType();
            final Class<?> actualRaw = actual.getRawType();
            if (formalRaw.isAssignableFrom(actualRaw)) {
                final TypeToken<?> superType = actual.getSupertype(Casts.cast(formalRaw));
                final ParameterizedType actualParameterized = (ParameterizedType) superType.getType();
                final ParameterizedType formalParameterized = (ParameterizedType) formal.getType();
                final Type[] actualTypeArgs = actualParameterized.getActualTypeArguments();
                final Type[] formalTypeArgs = formalParameterized.getActualTypeArguments();
                for (int i = 0; i < actualTypeArgs.length; i++) {
                    resolve(passed, map, of(formalTypeArgs[i]), of(actualTypeArgs[i]));
                }
            }
        } else if (formal.getType() instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType) formal.getType();
            for (final Type bound : wildcardType.getUpperBounds()) {
                resolve(passed, map, of(bound), actual);
            }
        }
    }

    public static Stream<TypeVariable<?>> typeVariables(Type type) {
        if (type instanceof TypeVariable<?>) {
            final TypeVariable<?> var = (TypeVariable<?>) type;
            return Stream.concat(
                    Stream.of(var),
                    Stream.of(var.getBounds()).flatMap(BeanTypeResolver::typeVariables)
            );
        } else if (type instanceof GenericArrayType) {
            return typeVariables(((GenericArrayType) type).getGenericComponentType());
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType pt = (ParameterizedType) type;
            return Stream.of(pt.getActualTypeArguments()).flatMap(BeanTypeResolver::typeVariables);
        } else if (type instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType) type;
            return Stream.concat(
                    Stream.of(wildcardType.getUpperBounds()).flatMap(BeanTypeResolver::typeVariables),
                    Stream.of(wildcardType.getLowerBounds()).flatMap(BeanTypeResolver::typeVariables)
            );
        } else {
            return Stream.empty();
        }
    }

    public static Type ground(Type type) {
        return typeVariables(type).distinct().reduce(
                new TypeResolver(),
                BeanTypeResolver::upper,
                (t1, t2) -> t2
        ).resolveType(type);
    }

    private static TypeResolver upper(TypeResolver resolver, TypeVariable<?> variable) {
        final Stream<Type> bounds = Stream.of(variable.getBounds());
        return resolver.where(
                variable,
                bounds.map(BeanTypeResolver::ground).filter(b -> b != Object.class).findFirst().orElse(Object.class)
        );
    }
}
