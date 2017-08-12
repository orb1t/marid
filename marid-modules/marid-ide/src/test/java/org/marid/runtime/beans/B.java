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

package org.marid.runtime.beans;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class B extends Bean {

    public B() {
    }

    public B(@Nonnull String name,
             @Nonnull String factory,
             @Nonnull String signature,
             @Nonnull BeanMethodArg... args) {
        super(name, factory, signature, args);
    }

    public B(@Nonnull String name,
             @Nonnull String factory,
             @Nonnull Constructor<?> constructor,
             @Nonnull BeanMethodArg... args) {
        super(name, factory, constructor, args);
    }

    public B(@Nonnull String name,
             @Nonnull String factory,
             @Nonnull Method method,
             @Nonnull BeanMethodArg... args) {
        super(name, factory, method, args);
    }

    public B(@Nonnull String name,
             @Nonnull String factory,
             @Nonnull Field field,
             @Nonnull BeanMethodArg... args) {
        super(name, factory, field, args);
    }
}
