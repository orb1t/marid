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

package org.marid.meta;

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import org.marid.misc.Casts;
import org.marid.runtime.context.MaridPlaceholderResolver;
import org.marid.runtime.model.MaridBean;
import org.marid.runtime.types.TypeContext;
import org.marid.runtime.types.TypeEvaluator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.function.Function;

public class GuavaTypeContext implements TypeContext {

    public static final Type WILDCARD;

    static {
        try {
            final Method method = TypeToken.class.getMethod("of", Type.class);
            final ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
            WILDCARD = parameterizedType.getActualTypeArguments()[0];
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    private final MaridBean bean;
    private final MaridPlaceholderResolver placeholderResolver;

    public GuavaTypeContext(MaridBean bean, ClassLoader classLoader, Properties properties) {
        this.bean = bean;
        this.placeholderResolver = new MaridPlaceholderResolver(classLoader, properties);
    }

    @Nonnull
    @Override
    public Type getWildcard() {
        return WILDCARD;
    }

    @Nonnull
    @Override
    public Type getBeanType(@Nonnull String name) {
        return bean.matchingCandidates()
                .filter(b -> name.equals(b.getName()))
                .findFirst()
                .map(b -> b.getFactory().getType(null, this))
                .orElse(WILDCARD);
    }

    @Nonnull
    @Override
    public Type resolve(@Nullable Type owner, @Nonnull Type type) {
        return owner == null ? type : TypeToken.of(owner).resolveType(type).getType();
    }

    @Nonnull
    @Override
    public String resolvePlaceholders(@Nonnull String value) {
        return placeholderResolver.resolvePlaceholders(value);
    }

    @Nonnull
    @Override
    public Class<?> getRaw(@Nonnull Type type) {
        return TypeToken.of(type).getRawType();
    }

    @Override
    public boolean isAssignable(@Nonnull Type from, @Nonnull Type to) {
        return TypeToken.of(from).isSubtypeOf(to);
    }

    @Nonnull
    @Override
    public ClassLoader getClassLoader() {
        return placeholderResolver.getClassLoader();
    }

    @Nonnull
    @Override
    public Type getClassType(@Nonnull Class<?> type) {
        final ParameterizedType parameterizedType = (ParameterizedType) getType(Class.class);
        return new TypeResolver()
                .where(parameterizedType.getActualTypeArguments()[0], type)
                .resolveType(parameterizedType);
    }

    @Nonnull
    @Override
    public Type getType(@Nonnull Class<?> type) {
        final TypeToken<?> t = TypeToken.of(type);
        final TypeToken<?> c = t.getSupertype(Casts.cast(type));
        return c.getType();
    }

    @Override
    public <T> T evaluate(@Nonnull Function<TypeEvaluator, T> callback) {
        return callback.apply(new GuavaTypeEvaluator());
    }
}
