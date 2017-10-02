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

package org.marid.type;

import com.google.common.reflect.TypeToken;
import org.marid.misc.Casts;
import org.marid.runtime.expression.*;
import org.marid.runtime.model.MaridBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class GuavaTypeResolver implements TypeResolver {

    private final ClassLoader classLoader;

    public GuavaTypeResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Nonnull
    @Override
    public Type resolve(@Nonnull MaridBean bean, @Nullable Type owner, @Nonnull Expression expression) {
        final Class<?> type = expression.getClass();
        try {
            final Method method = getClass().getDeclaredMethod("resolve", MaridBean.class, Type.class, type);
            return (Type) method.invoke(this, bean, owner, expression);
        } catch (ReflectiveOperationException e) {
            return WILDCARD;
        }
    }

    private Type resolve(MaridBean bean, Type owner, StringExpression expression) {
        return String.class;
    }

    private Type resolve(MaridBean bean, Type owner, IntegerExpression expression) {
        return Integer.class;
    }

    private Type resolve(MaridBean bean, Type owner, RefExpression expression) {
        return bean.matchingCandidates()
                .filter(b -> b.getName().equals(expression.getReference()))
                .findFirst()
                .map(this::resolve)
                .map(t -> resolve(owner, t))
                .orElse(WILDCARD);
    }

    private Type resolve(MaridBean bean, Type owner, ClassExpression expression) {
        try {
            final Class<?> c = classLoader.loadClass(expression.getClassName());
            final Type type = TypeToken.of(c).getSupertype(Casts.cast(c)).getType();
            return resolve(owner, type);
        } catch (Throwable x) {
            return WILDCARD;
        }
    }

    private Type resolve(MaridBean bean, Type owner, FieldSetExpression expression) {
        return resolve(bean, owner, expression.getTarget());
    }

    private Type resolve(MaridBean bean, Type owner, FieldSetStaticExpression expression) {
        return Void.class;
    }

    private Type resolve(Type owner, Type type) {
        return owner == null ? type : TypeToken.of(owner).resolveType(type).getType();
    }
}
