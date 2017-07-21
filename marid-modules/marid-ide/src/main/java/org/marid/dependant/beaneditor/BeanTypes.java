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

package org.marid.dependant.beaneditor;

import org.marid.ide.model.BeanMethodArgData;
import org.marid.ide.types.BeanTypeResolver;
import org.marid.ide.types.BeanTypeResolverContext;

import java.lang.reflect.Type;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTypes {

    private final BeanTypeResolverContext resolverContext;
    private final BeanTypeResolver typeResolver;
    private final BeanMethodArgData arg;

    public BeanTypes(BeanTypeResolverContext resolverContext, BeanTypeResolver resolver, BeanMethodArgData arg) {
        this.resolverContext = resolverContext;
        this.typeResolver = resolver;
        this.arg = arg;
    }

    public boolean isConstructorArg() {
        return arg.parent == arg.parent.parent.getProducer();
    }

    public Type getBeanType() {
        return typeResolver.resolve(resolverContext, arg.parent.parent.getName());
    }

    public Type getActualArgType() {
        final Type beanType = getBeanType();
        final int argIndex = arg.parent.args.indexOf(arg);
        return null;
    }
}
