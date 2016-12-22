/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.dependant.beaneditor.beandata;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.beans.BeanIntrospector;
import org.marid.beans.ClassInfo;
import org.marid.beans.MethodInfo;
import org.marid.beans.TypeInfo;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.AbstractData;
import org.marid.spring.xml.BeanData;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class BeanInfo {

    public final ObservableList<MethodInfo> constructors = FXCollections.observableArrayList();
    public final ObservableList<MethodInfo> methods = FXCollections.observableArrayList();
    public final ObservableList<TypeInfo> properties = FXCollections.observableArrayList();

    private final ProjectProfile profile;
    private final InvalidationListener profileInvalidationListener;
    private final InvalidationListener beanInfoListener;

    public BeanInfo(ProjectProfile profile, BeanData beanData) {
        this.profile = profile;
        this.profileInvalidationListener = o -> {
            final ResolvableType type = profile.getType(beanData);
            if (type != ResolvableType.NONE) {
                final ClassLoader classLoader = profile.getClassLoader();
                final ClassInfo classInfo = BeanIntrospector.classInfo(classLoader, type);
                if (beanData.isFactoryBean()) {
                    final Optional<BeanData> factoryBean = profile.findBean(beanData.factoryBean.getValueSafe());
                    if (factoryBean.isPresent()) {
                        final ResolvableType factoryBeanType = profile.getType(factoryBean.get());
                        if (factoryBeanType != ResolvableType.NONE) {
                            final ClassInfo factoryClassInfo = BeanIntrospector.classInfo(classLoader, factoryBeanType);
                            constructors.setAll(Stream.of(factoryClassInfo.methodInfos)
                                    .filter(i -> i.name.equals(beanData.factoryMethod.getValue()))
                                    .collect(Collectors.toList()));
                        }
                    }
                } else {
                    constructors.setAll(classInfo.constructorInfos);
                }
                methods.setAll(classInfo.methodInfos);
                properties.setAll(classInfo.propertyInfos);
            }
        };
        this.beanInfoListener = o -> {
            beanData.beanArgs.forEach(AbstractData::invalidate);
            beanData.properties.forEach(AbstractData::invalidate);
        };
    }

    @PostConstruct
    private void init() {
        profileInvalidationListener.invalidated(profile);
        profile.addListener(profileInvalidationListener);
        profile.addListener(beanInfoListener);
    }

    @PreDestroy
    private void destroy() {
        profile.removeListener(profileInvalidationListener);
        profile.removeListener(beanInfoListener);
    }
}
