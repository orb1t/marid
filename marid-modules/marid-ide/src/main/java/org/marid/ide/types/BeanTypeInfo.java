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
public class BeanTypeInfo {

    private final Type type;
    private final Type[] parameters;
    private final Type[] arguments;
    private final Type[][] initializerParameters;
    private final Type[][] initializerArguments;

    public BeanTypeInfo(Type type,
                        Type[] parameters,
                        Type[] arguments,
                        Type[][] initializerParameters,
                        Type[][] initializerArguments) {
        this.type = type;
        this.parameters = parameters;
        this.arguments = arguments;
        this.initializerParameters = initializerParameters;
        this.initializerArguments = initializerArguments;
    }

    public Type getType() {
        return type;
    }

    public Type[] getParameters(BeanMethodData producer) {
        return producer.parent.getProducer() == producer
                ? parameters
                : initializerParameters[producer.parent.initializers.indexOf(producer)];
    }

    public Type[] getArguments(BeanMethodData producer) {
        return producer.parent.getProducer() == producer
                ? arguments
                : initializerArguments[producer.parent.initializers.indexOf(producer)];
    }

    public Type getParameter(BeanMethodArgData parameter) {
        return getParameters(parameter.parent)[parameter.parent.args.indexOf(parameter)];
    }

    public Type getArgument(BeanMethodArgData argument) {
        return getArguments(argument.parent)[argument.parent.args.indexOf(argument)];
    }
}
