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
import org.marid.ide.project.ProjectProfile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.marid.misc.Calls.call;
import static org.marid.runtime.beans.Bean.ref;
import static org.marid.runtime.beans.Bean.type;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTypeResolver {

    public Type resolve(List<BeanData> beans, ClassLoader classLoader, String beanName) {
        final BeanData beanData = beans.stream()
                .filter(e -> beanName.equals(e.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(beanName));
        final IdeValueConverterManager valueConverters = new IdeValueConverterManager(classLoader);

        final Type factoryType;
        final Class<?> factoryClass;
        if (ref(beanData.getFactory()) != null) {
            factoryType = resolve(beans, classLoader, ref(beanData.getFactory()));
            factoryClass = TypeToken.of(factoryType).getRawType();
        } else {
            factoryType = factoryClass = resolveClassName(requireNonNull(type(beanData.getFactory())), classLoader);
        }

        final MethodHandle producerHandle = call(() -> beanData.toInfo().findProducer(factoryClass));
        final Member producerMember = call(() -> MethodHandles.reflectAs(Member.class, producerHandle));

        final Type[] actualArgTypes = new Type[beanData.getArgs().size()];
        for (int i = 0; i < actualArgTypes.length; i++) {
            final BeanMemberData beanArg = beanData.getArgs().get(i);
            switch (beanArg.getType()) {
                case "ref":
                    actualArgTypes[i] = resolve(beans, classLoader, beanArg.getValue());
                    break;
                default:
                    actualArgTypes[i] = valueConverters.getType(beanArg.getType()).orElse(null);
                    break;
            }
        }

        return MaridTypes.resolveType(producerMember, factoryType, actualArgTypes);
    }

    public Type resolve(ProjectProfile profile, String beanName) {
        return resolve(profile.getBeansFile().beans, profile.getClassLoader(), beanName);
    }
}
