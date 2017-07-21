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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.reflect.TypeToken.of;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
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
                final Map<TypeToken<?>, List<TypeToken<?>>> pairs = new LinkedHashMap<>();
                final Type[] beanPs = formalTypes(info.returnHandle, true);
                final Type[] beanAs = info.producer.args.stream().map(a -> actualType(context, a)).toArray(Type[]::new);
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
                    final Type[] as = beanData.getArgs(i).map(a -> actualType(context, a)).toArray(Type[]::new);
                    for (int k = 0; k < as.length; k++) {
                        if (as[k] != null) {
                            resolve(pairs, info.factoryToken.resolveType(ps[k]), of(as[k]));
                        }
                    }
                    initPs[i] = ps;
                    initAs[i] = range(0, as.length).mapToObj(n -> as[n] != null ? as[n] : ps[n]).toArray(Type[]::new);
                }

                final TypeResolver r = pairs.entrySet().stream().reduce(
                        new TypeResolver(),
                        (a, e) -> a.where(e.getKey().getType(), commonAncestor(e).getType()),
                        (r1, r2) -> r2
                );
                final Type resolvedType = r.resolveType(info.returnType);
                final TypeToken<?> resolvedToken = info.factoryToken.resolveType(resolvedType);
                final Type type = resolvedToken.getType();

                return new BeanTypeInfo(type, beanPs, beanPs, initPs, initAs);
            } catch (RuntimeException x) {
                throw x;
            } catch (Exception x) {
                throw new IllegalArgumentException(name, x);
            } finally {
                context.processing.remove(name);
            }
        });
    }

    private TypeToken<?> commonAncestor(Entry<TypeToken<?>, List<TypeToken<?>>> entry) {
        final List<TypeToken<?>> tokens = entry.getValue();
        if (tokens.size() == 1 || tokens.stream().allMatch(t -> t.equals(tokens.get(0)))) {
            return tokens.get(0);
        } else {
            final TypeToken<?>[][] sets = tokens.stream()
                    .map(TypeToken::getTypes)
                    .map(s -> s.toArray(new TypeToken<?>[s.size()]))
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
                            arg.parent.toProducer().name(),
                            arg.getName(),
                            arg.getFilter());
                }
            }
        }
    }

    private void resolve(Map<TypeToken<?>, List<TypeToken<?>>> map, TypeToken<?> formal, TypeToken<?> actual) {
        if (formal.isArray() && actual.isArray()) {
            resolve(map, formal.getComponentType(), actual.getComponentType());
        } else if (formal.getType() instanceof TypeVariable<?>) {
            final TypeVariable<?> typeVariable = (TypeVariable<?>) formal.getType();
            for (final Type bound : typeVariable.getBounds()) {
                resolve(map, of(bound), actual);
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
                    resolve(map, of(formalTypeArgs[i]), of(actualTypeArgs[i]));
                }
            }
        } else if (formal.getType() instanceof WildcardType) {
            final WildcardType wildcardType = (WildcardType) formal.getType();
            for (final Type bound : wildcardType.getUpperBounds()) {
                resolve(map, of(bound), actual);
            }
        }
    }
}
