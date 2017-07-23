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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.dependant.beaneditor.dao.LibraryBeanDao;
import org.marid.dependant.beaneditor.model.LibraryBean;
import org.marid.ide.model.BeanData;
import org.marid.ide.model.BeanMethodArgData;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.types.BeanCache;
import org.marid.ide.types.BeanTypeInfo;
import org.marid.ide.types.BeanTypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class BeanEditorContext implements AutoCloseable {

    private final ProjectProfile profile;
    private final BeanTypeResolver resolver;
    private final LibraryBeanDao libraryBeanDao;

    public final ObservableList<LibraryBean> discoveredBeans = FXCollections.observableArrayList();

    private BeanCache discoveredContext;

    @Autowired
    public BeanEditorContext(ProjectProfile profile, BeanTypeResolver resolver, LibraryBeanDao libraryBeanDao) {
        this.profile = profile;
        this.resolver = resolver;
        this.libraryBeanDao = libraryBeanDao;

        profile.addOnUpdate(this::updateAsync);
        updateAsync(profile);
    }

    public Type formalType(BeanMethodArgData arg) {
        final BeanTypeInfo typeInfo = resolver.resolve(profile.getBeanCache(), arg.parent.parent.getName());
        return typeInfo.getParameter(arg);
    }

    public Type actualType(BeanMethodArgData arg) {
        final BeanTypeInfo typeInfo = resolver.resolve(profile.getBeanCache(), arg.parent.parent.getName());
        return typeInfo.getArgument(arg);
    }

    public Type possibleType(BeanMethodArgData arg) {
        final String oldValue = arg.getValue();
        arg.value.set(null);
        try {
            return actualType(arg);
        } finally {
            arg.value.set(oldValue);
        }
    }

    private void updateAsync(ProjectProfile profile) {
        CompletableFuture.runAsync(this::onProfileUpdate);
    }

    private void onProfileUpdate() {
        final LibraryBean[] libraryBeans = libraryBeanDao.beans();
        final ObservableList<BeanData> libraryBeansData = Stream.of(libraryBeans)
                .map(b -> new BeanData(b.bean))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        Platform.runLater(() -> {
            discoveredBeans.setAll(libraryBeans);
            discoveredContext = new BeanCache(libraryBeansData, profile.getClassLoader());
        });
    }

    @Override
    public void close() {
        profile.removeOnUpdate(this::updateAsync);
    }
}
