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
import java.util.Arrays;
import java.util.Formatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class GenericBeanTypeInfo implements BeanTypeInfo {

    private final Type type;
    private final Type[] parameters;
    private final Type[] arguments;
    private final Type[][] initializerParameters;
    private final Type[][] initializerArguments;

    public GenericBeanTypeInfo(UnaryOperator<Type> resolver,
                               Type type,
                               Type[] parameters,
                               Type[] arguments,
                               Type[][] initializerParameters,
                               Type[][] initializerArguments) {
        this.type = resolver.apply(type);
        this.parameters = parameters;
        this.arguments = arguments;
        this.initializerParameters = initializerParameters;
        this.initializerArguments = initializerArguments;

        for (int i = 0; i < arguments.length; i++) {
            final Type parameter = resolver.apply(parameters[i]);
            arguments[i] = arguments[i] == null ? parameter : resolver.apply(arguments[i]);
        }

        for (int i = 0; i < initializerArguments.length; i++) {
            final Type[] iParameters = initializerParameters[i];
            final Type[] iArguments = initializerArguments[i];
            for (int k = 0; k < iArguments.length; k++) {
                final Type p = resolver.apply(iParameters[k]);
                iArguments[k] = iArguments[k] == null ? p : resolver.apply(iArguments[k]);
            }
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Type[] getParameters(BeanMethodData producer) {
        if (producer.parent.getProducer() == producer) {
            return parameters;
        } else {
            final int index = producer.parent.initializers.indexOf(producer);
            return index < 0
                    ? Stream.generate(() -> Void.class).limit(producer.args.size()).toArray(Type[]::new)
                    : initializerParameters[index];
        }
    }

    @Override
    public Type[] getArguments(BeanMethodData producer) {
        if (producer.parent.getProducer() == producer) {
            return arguments;
        } else {
            final int index = producer.parent.initializers.indexOf(producer);
            return index < 0
                    ? Stream.generate(() -> Void.class).limit(producer.args.size()).toArray(Type[]::new)
                    : initializerArguments[index];
        }
    }

    @Override
    public Type getParameter(BeanMethodArgData parameter) {
        return getParameters(parameter.parent)[parameter.parent.args.indexOf(parameter)];
    }

    @Override
    public Type getArgument(BeanMethodArgData argument) {
        return getArguments(argument.parent)[argument.parent.args.indexOf(argument)];
    }

    @Override
    public Throwable getError() {
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        final AtomicBoolean empty = new AtomicBoolean(true);
        try (final Formatter formatter = new Formatter(builder)) {
            formatter.format("%s{", type);
            if (parameters.length > 0) {
                if (empty.compareAndSet(true, false)) formatter.format("%n");
                formatter.format("\t%s%n", Arrays.toString(parameters));
                formatter.format("\t%s%n", Arrays.toString(arguments));
            }
            for (int i = 0; i < initializerParameters.length; i++) {
                if (initializerParameters[i].length > 0) {
                    if (empty.compareAndSet(true, false)) formatter.format("%n");
                    formatter.format("\t%s%n", Arrays.toString(initializerParameters[i]));
                    formatter.format("\t%s%n", Arrays.toString(initializerArguments[i]));
                }
            }
            formatter.format("}");
        }
        return builder.toString();
    }
}
