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

import com.google.common.collect.ComputationException;
import com.google.common.reflect.TypeToken;
import org.marid.ide.model.BeanData;
import org.marid.misc.Casts;
import org.marid.runtime.beans.Bean;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import static com.google.common.reflect.TypeToken.of;
import static java.util.Objects.requireNonNull;
import static org.marid.l10n.L10n.m;
import static org.marid.runtime.beans.Bean.ref;
import static org.marid.runtime.beans.Bean.type;
import static org.springframework.util.ClassUtils.resolveClassName;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFactoryInfo {

    @Nonnull
    public final Bean bean;

    @Nonnull
    public final String factory;

    @Nonnull
    public final Class<?> factoryClass;

    @Nonnull
    public final TypeToken<?> factoryToken;

    @Nonnull
    public final MethodHandle returnHandle;

    @Nonnull
    public final Member returnMember;

    @Nonnull
    public final Class<?> returnClass;

    @Nonnull
    public final Type genericReturnType;

    @Nonnull
    public final TypeToken<?> genericReturnToken;

    @Nonnull
    public final Type returnType;

    public BeanFactoryInfo(BeanData beanData, BeanTypeResolver resolver, BeanTypeResolverContext context) {
        try {
            bean = beanData.toBean();
            factory = requireNonNull(beanData.getFactory(), () -> m("Factory is null: {0}", bean.name));
            if (ref(factory) != null) {
                factoryToken = of(resolver.resolve(context, ref(factory)));
                factoryClass = factoryToken.getRawType();
            } else {
                final String className = requireNonNull(type(factory), () -> m("Factory class is null: {0}", bean.name));
                factoryClass = resolveClassName(className, context.getClassLoader());
                factoryToken = of(factoryClass).getSupertype(Casts.cast(factoryClass));
            }
            returnHandle = bean.findProducer(factoryClass);
            returnMember = MethodHandles.reflectAs(Member.class, returnHandle);
            returnClass = returnHandle.type().returnType();
            genericReturnType = returnMember instanceof Field
                    ? ((Field) returnMember).getGenericType()
                    : ((Executable) returnMember).getAnnotatedReturnType().getType();
            genericReturnToken = genericReturnType instanceof Class<?>
                    ? of(genericReturnType).getSupertype(Casts.cast(returnClass))
                    : of(genericReturnType);
            returnType = factoryToken.resolveType(genericReturnToken.getType()).getType();
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            throw new ComputationException(x);
        }
    }
}
