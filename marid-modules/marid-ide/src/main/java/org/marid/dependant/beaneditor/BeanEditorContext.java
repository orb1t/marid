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
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.types.BeanTypeInfo;
import org.marid.ide.types.BeanTypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class BeanEditorContext {

    private final ProjectProfile profile;
    private final BeanTypeResolver resolver;

    @Autowired
    public BeanEditorContext(ProjectProfile profile, BeanTypeResolver resolver) {
        this.profile = profile;
        this.resolver = resolver;
    }

    public Type formalType(BeanMethodArgData arg) {
        final BeanTypeInfo typeInfo = resolver.resolve(profile.getBeanContext(), arg.parent.parent.getName());
        return typeInfo.getParameter(arg);
    }

    public Type actualType(BeanMethodArgData arg) {
        final BeanTypeInfo typeInfo = resolver.resolve(profile.getBeanContext(), arg.parent.parent.getName());
        return typeInfo.getArgument(arg);
    }
}
