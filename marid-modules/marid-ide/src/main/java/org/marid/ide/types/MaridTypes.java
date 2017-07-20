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

import java.lang.reflect.*;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridTypes {

    static Type resolveType(Member member, Type owner, Type... parameters) {
        if (member instanceof Constructor<?>) {
            final Constructor<?> constructor = (Constructor<?>) member;
            final Type[] arguments = constructor.getGenericParameterTypes();
            if (arguments.length != parameters.length) {
                return owner;
            }
            final Class<?> c = (Class<?>) owner;
            final TypeVariable<?>[] vars = c.getTypeParameters();
            if (vars.length == 0) {
                return owner;
            }


        } else if (member instanceof Method) {
            final Method method = (Method) member;
            return method.getGenericReturnType();
        } else {
            return owner;
        }
        return null;
    }
}
