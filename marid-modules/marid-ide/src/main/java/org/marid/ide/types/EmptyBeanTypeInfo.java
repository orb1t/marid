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

import org.marid.ide.model.BeanMethodArgData;
import org.marid.ide.model.BeanMethodData;

import java.lang.reflect.Type;

/**
 * @author Dmitry Ovchinnikov
 */
public class EmptyBeanTypeInfo implements BeanTypeInfo {

    private final Throwable error;

    public EmptyBeanTypeInfo(Throwable error) {
        this.error = error;
    }

    @Override
    public Type getType() {
        return Object.class;
    }

    @Override
    public Type[] getParameters(BeanMethodData producer) {
        return producer.args.stream().map(a -> Object.class).toArray(Type[]::new);
    }

    @Override
    public Type[] getArguments(BeanMethodData producer) {
        return producer.args.stream().map(a -> Object.class).toArray(Type[]::new);
    }

    @Override
    public Type getParameter(BeanMethodArgData parameter) {
        return Object.class;
    }

    @Override
    public Type getArgument(BeanMethodArgData argument) {
        return Object.class;
    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public String toString() {
        return "<empty>";
    }
}
