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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public interface TypeUtilities {

    static Type classType(ClassLoader classLoader, String name) {
        try {
            final Class<?> c = Class.forName(name, false, classLoader);
            final Type t = TypeToken.of(Class.class).getSupertype(Class.class).getType();
            final ParameterizedType pt = (ParameterizedType) t;
            return new TypeResolver().where(pt.getActualTypeArguments()[0], c).resolveType(t);
        } catch (ClassNotFoundException x) {
            log(WARNING, "Unable to load class {0}", x, name);
            return null;
        }
    }
}
