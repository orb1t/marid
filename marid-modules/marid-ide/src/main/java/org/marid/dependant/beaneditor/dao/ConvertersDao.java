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

package org.marid.dependant.beaneditor.dao;

import org.marid.annotation.MetaLiteral;
import org.marid.dependant.beaneditor.BeanEditorContext;
import org.marid.ide.model.BeanMethodArgData;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class ConvertersDao {

    private final ProjectProfile profile;
    private final BeanEditorContext context;

    @Autowired
    public ConvertersDao(ProjectProfile profile, BeanEditorContext context) {
        this.profile = profile;
        this.context = context;
    }

    public Map<String, MetaLiteral> getConverters(BeanMethodArgData arg) {
        final Type type = context.formalType(arg);
        return profile.getBeanContext().getConverters().getMatchedConverters(type);
    }
}
