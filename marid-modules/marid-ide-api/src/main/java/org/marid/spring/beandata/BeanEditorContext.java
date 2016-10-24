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

package org.marid.spring.beandata;

import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanFile;

import java.lang.reflect.Type;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
public interface BeanEditorContext {

    URLClassLoader getClassLoader();

    boolean containBean(String name);

    String generateBeanName(String name);

    boolean isHmi();

    ObservableList<Pair<Path, BeanFile>> getBeanFiles();

    String getProfileName();

    Optional<Class<?>> getType(String type);

    BeanData getBeanData();

    Optional<? extends Type> getType(BeanData beanData);

    Optional<Class<?>> getClass(BeanData beanData);

    Class<?> getType();
}
