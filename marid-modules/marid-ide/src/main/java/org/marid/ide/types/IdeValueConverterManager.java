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

import org.apache.commons.lang3.reflect.TypeUtils;
import org.marid.annotation.MetaLiteral;
import org.marid.runtime.context.MaridRuntime;
import org.marid.runtime.converter.DefaultValueConvertersManager;
import org.marid.runtime.converter.ValueConverters;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.TreeMap;

import static com.google.common.reflect.Reflection.newProxy;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeValueConverterManager extends DefaultValueConvertersManager {

    public IdeValueConverterManager(ClassLoader classLoader) {
        super(classLoader, newProxy(MaridRuntime.class, (proxy, method, args) -> null));
    }

    public TreeMap<String, MetaLiteral> getMatchedConverters(Type target) {
        final TreeMap<String, MetaLiteral> result = new TreeMap<>();
        for (final ValueConverters c : valueConverters) {
            c.getMetaMap().forEach((name, literal) -> {
                final Type type = c.getTypeMap().get(name);
                if (type == null
                        || TypeUtils.isAssignable(type, target)
                        || type instanceof WildcardType
                        || target instanceof TypeVariable<?>) {
                    result.put(name, literal);
                }
            });
        }
        return result;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
