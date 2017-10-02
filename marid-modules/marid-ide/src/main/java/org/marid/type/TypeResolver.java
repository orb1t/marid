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
import org.marid.misc.Calls;
import org.marid.runtime.expression.Expression;
import org.marid.runtime.model.MaridBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface TypeResolver {

    Type WILDCARD = Calls.call(() -> {
        final Type t = TypeToken.class.getMethod("of", Type.class).getGenericReturnType();
        final ParameterizedType pt = (ParameterizedType) t;
        return pt.getActualTypeArguments()[0];
    });

    @Nonnull
    Type resolve(@Nonnull MaridBean bean, @Nullable Type owner, @Nonnull Expression expression);

    @Nonnull
    default Type resolve(@Nonnull MaridBean bean) {
        return resolve(bean, null, bean.getFactory());
    }
}
