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
import org.marid.ide.model.BeanData;
import org.marid.ide.model.BeanMemberData;
import org.marid.ide.model.BeanProducerData;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.types.BeanTypeResolver.TypePair;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTypeResolverContext {

    final IdeValueConverterManager converters;
    final Map<String, BeanData> beanMap;
    final Map<String, Type> resolved = new HashMap<>();
    final LinkedHashSet<String> processing = new LinkedHashSet<>();
    final Map<String, BeanFactoryInfo> factoryMap = new HashMap<>();

    public BeanTypeResolverContext(Iterable<BeanData> beans, ClassLoader classLoader) {
        converters = new IdeValueConverterManager(classLoader);
        beanMap = stream(beans.spliterator(), false).collect(toMap(BeanData::getName, identity()));
    }

    public BeanTypeResolverContext(ProjectProfile profile) {
        this(profile.getBeansFile().beans, profile.getClassLoader());
    }

    public BeanData getBean(String name) {
        return beanMap.get(name);
    }

    public BeanFactoryInfo factory(String name) {
        return factoryMap.get(name);
    }

    public void reset(String name) {
        resolved.remove(name);
        factoryMap.remove(name);
    }

    ClassLoader getClassLoader() {
        return converters.getClassLoader();
    }

    void fill(BeanTypeResolver resolver,
                     List<TypePair> pairs,
                     MethodHandle handle,
                     BeanProducerData producerData,
                     TypeToken<?> factoryToken) throws Exception {
        final Member member = MethodHandles.reflectAs(Member.class, handle);
        final Type[] formalTypes = member instanceof Field
                ? new Type[] {((Field) member).getGenericType()}
                : ((Executable) member).getGenericParameterTypes();
        if (formalTypes.length != producerData.args.size()) {
            return;
        }

        final Type[] actualTypes = new Type[formalTypes.length];
        for (int i = 0; i < actualTypes.length; i++) {
            final BeanMemberData beanArg = producerData.args.get(i);
            switch (beanArg.getType()) {
                case "ref":
                    actualTypes[i] = resolver.resolve(this, beanArg.getValue());
                    break;
                default:
                    actualTypes[i] = converters.getType(beanArg.getType()).orElse(null);
                    break;
            }
        }
        IntStream.range(0, formalTypes.length)
                .filter(i -> actualTypes[i] != null)
                .mapToObj(i -> new TypePair(actualTypes[i], factoryToken.resolveType(formalTypes[i])))
                .forEach(pairs::add);
    }
}
