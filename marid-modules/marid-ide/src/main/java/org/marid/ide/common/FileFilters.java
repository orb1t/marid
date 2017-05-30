/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.common;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class FileFilters {

    @Bean
    @Qualifier("java")
    public PathMatcher javaPathMatcher() {
        return FileSystems.getDefault().getPathMatcher("glob:*.java");
    }

    @Bean
    @Qualifier("jar")
    public PathMatcher jarPathMatcher() {
        return FileSystems.getDefault().getPathMatcher("glob:*.jar");
    }

    @Bean
    @Qualifier("properties")
    public PathMatcher propertiesPathMatcher() {
        return FileSystems.getDefault().getPathMatcher("glob:*.properties");
    }

    @Bean
    @Qualifier("lst")
    public PathMatcher confListPathMatcher() {
        return FileSystems.getDefault().getPathMatcher("glob:*.lst");
    }
}
