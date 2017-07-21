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

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

import static com.google.common.reflect.TypeToken.of;

/**
 * @author Dmitry Ovchinnikov
 */
class TypePair {

    final TypeToken<?> actual;
    final TypeToken<?> formal;

    TypePair(TypeToken<?> actual, TypeToken<?> formal) {
        this.actual = actual;
        this.formal = formal;
    }

    TypePair(Type actual, Type formal) {
        this(of(actual), of(formal));
    }

    TypePair(Type actual, TypeToken<?> formal) {
        this(of(actual), formal);
    }
}
