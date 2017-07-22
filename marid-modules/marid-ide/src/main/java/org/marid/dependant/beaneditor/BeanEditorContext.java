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

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.marid.annotation.MetaLiteral;
import org.marid.ide.model.BeanMethodArgData;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.types.BeanTypeInfo;
import org.marid.ide.types.BeanTypeResolver;
import org.marid.ide.types.BeanTypeResolverContext;
import org.marid.runtime.annotation.MaridBean;
import org.marid.runtime.beans.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class BeanEditorContext {

    private final ProjectProfile profile;
    private final BeanTypeResolver resolver;
    private final ObservableList<Pair<Bean, MetaLiteral>> discoveredBeans = FXCollections.observableArrayList();

    private BeanTypeResolverContext context;
    private BeanTypeResolverContext discoveredContext;

    @Autowired
    public BeanEditorContext(ProjectProfile profile, BeanTypeResolver resolver) {
        this.profile = profile;
        this.resolver = resolver;
        this.context = new BeanTypeResolverContext(profile);
        this.discoveredContext = new BeanTypeResolverContext(profile);

        profile.enabledProperty().addListener(this::onProfileEnabledChange);
        CompletableFuture.runAsync(this::onProfileUpdate);
    }

    public Type formalType(BeanMethodArgData arg) {
        final BeanTypeInfo typeInfo = resolver.resolveInfo(context, arg.parent.parent.getName());
        return typeInfo.getParameter(arg);
    }

    public Type actualType(BeanMethodArgData arg) {
        final BeanTypeInfo typeInfo = resolver.resolveInfo(context, arg.parent.parent.getName());
        return typeInfo.getArgument(arg);
    }

    public Type possibleType(BeanMethodArgData arg) {
        context.reset(arg.parent.parent.getName());
        final String oldValue = arg.getValue();
        arg.value.set(null);
        try {
            return actualType(arg);
        } finally {
            arg.value.set(oldValue);
        }
    }

    private void onProfileEnabledChange(Observable observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
            CompletableFuture.runAsync(this::onProfileUpdate);
        }
    }

    private void onProfileUpdate() {
        context = new BeanTypeResolverContext(profile);
        discoveredContext = new BeanTypeResolverContext(profile);

        final ClassPathScanningCandidateComponentProvider p = new ClassPathScanningCandidateComponentProvider(false);
        p.setResourceLoader(new PathMatchingResourcePatternResolver(context.getClassLoader()));
        p.addIncludeFilter(new AnnotationTypeFilter(MaridBean.class));
    }
}
