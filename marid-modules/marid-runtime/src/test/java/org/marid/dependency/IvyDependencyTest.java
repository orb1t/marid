/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.dependency;

import com.google.common.collect.ImmutableMap;
import groovy.grape.GrapeIvy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.groovy.GroovyRuntime;
import org.marid.test.NormalTests;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class IvyDependencyTest {

    @Test
    public void testSlf4jDependency() throws Exception {
        final GrapeIvy ivy = new GrapeIvy();
        final Map<?, ?> args = new LinkedHashMap<>(ImmutableMap.of("classLoader", GroovyRuntime.CLASS_LOADER, "autoDownload", true));
        final Map<?, ?> dep1 = ImmutableMap.of("groupId", "org.slf4j", "artifactId", "slf4j-api", "version", "1.7.12");
        ivy.grab(args, dep1);
        final Class<?> c = GroovyRuntime.CLASS_LOADER.loadClass("org.slf4j.LoggerFactory");
        Assert.assertEquals("LoggerFactory", c.getSimpleName());
    }
}
