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
import org.marid.misc.Casts;
import org.marid.runtime.beans.Bean;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import static com.google.common.reflect.TypeToken.of;
import static org.marid.runtime.context.MaridRuntimeUtils.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFactoryInfo {

    @Nonnull
    public final Bean bean;

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

    public BeanFactoryInfo(BeanData beanData, BeanTypeResolver resolver, BeanContext context) throws Exception {
        bean = beanData.toBean();
        returnMember = fromSignature(beanData.getSignature(), context.getClassLoader());
        returnHandle = producer(returnMember);
        returnClass = returnHandle.type().returnType();
        if (isRoot(returnMember)) {
            factoryToken = of(returnMember.getDeclaringClass()).getSupertype(Casts.cast(returnMember.getDeclaringClass()));
        } else {
            factoryToken = of(resolver.resolve(context, beanData, beanData.getFactory()).getType());
        }
        genericReturnType = returnMember instanceof Field
                ? ((Field) returnMember).getGenericType()
                : ((Executable) returnMember).getAnnotatedReturnType().getType();
        genericReturnToken = genericReturnType instanceof Class<?>
                ? of(genericReturnType).getSupertype(Casts.cast(returnClass))
                : of(genericReturnType);
        returnType = factoryToken.resolveType(genericReturnToken.getType()).getType();
    }
}
