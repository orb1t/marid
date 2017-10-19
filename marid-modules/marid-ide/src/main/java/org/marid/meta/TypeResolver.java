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

import org.marid.beans.MaridBean;
import org.marid.expression.generic.Expression;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;

public interface TypeResolver {

    @Nonnull
    Type resolve(@Nonnull MaridBean base, @Nonnull TypeResolverContext context, @Nonnull Expression expression);

    @Nonnull
    default Type resolve(@Nonnull MaridBean bean, @Nonnull TypeResolverContext context) {
        return resolve(bean, context, bean.getFactory());
    }
}
