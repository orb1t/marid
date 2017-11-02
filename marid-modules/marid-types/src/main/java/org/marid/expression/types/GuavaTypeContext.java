/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.expression.types;

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;
import org.marid.beans.MaridBean;
import org.marid.misc.Casts;
import org.marid.runtime.context.MaridPlaceholderResolver;
import org.marid.runtime.types.TypeContext;
import org.marid.runtime.types.TypeEvaluator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

public class GuavaTypeContext implements TypeContext {

    public static final Type WILDCARD = Stream.of(TypeToken.class.getMethods())
            .filter(m -> "of".equals(m.getName()) && m.getParameterTypes()[0] == Type.class)
            .map(m -> ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0])
            .findFirst()
            .orElseThrow(IllegalStateException::new);

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
