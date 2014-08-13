/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.ide.profile;

import groovy.lang.Script;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.logging.LogSupport;
import org.marid.nio.FileUtils;
import org.marid.test.NormalTests;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class ProfilesTest implements LogSupport {

    private static Path dir;

    @BeforeClass
    public static void init() throws Exception {
        dir = Files.createTempDirectory("profiles");
        Log.info("Created directory: {0}", dir);
    }

    @AfterClass
    public static void destroy() throws Exception {
        Files.walkFileTree(dir, FileUtils.RECURSIVE_CLEANER);
        Log.info("Cleaned {0}", dir);
    }

    @Test
    public void testUpdateProfile() throws Exception {
        final Profile profile = new Profile(dir.resolve("profile1"));
        Assert.assertEquals("profile1", profile.getName());
        final Path classesPath = profile.classesPath;
        Files.write(classesPath.resolve("Test1.groovy"), "1".getBytes(UTF_8));
        final Class<?> test1Class = profile.loadClass("Test1");
        info("Loaded class: {0} {1}", test1Class, System.identityHashCode(test1Class));
        final Script script1 = (Script) test1Class.newInstance();
        Assert.assertEquals(1, ((Number) script1.run()).intValue());
        profile.update();
        Files.write(classesPath.resolve("Test1.groovy"), "2".getBytes(UTF_8));
        final Class<?> test2Class = profile.loadClass("Test1");
        info("Loaded class: {0} {1}", test2Class, System.identityHashCode(test2Class));
        final Script script2 = (Script) test2Class.newInstance();
        Assert.assertEquals(2, ((Number) script2.run()).intValue());
    }
}
