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

package org.marid.repo;

import groovy.grape.Grape;
import groovy.lang.GroovyClassLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.groovy.GroovyRuntime;
import org.marid.logging.LogSupport;
import org.marid.test.ManualTests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({ManualTests.class})
public class GrapesTest implements LogSupport {

    private static final File maridDir = new File(System.getProperty("user.home"), "marid");
    private static final File profilesDir = new File(maridDir, "profiles");
    private static final File testProfileDir = new File(profilesDir, "grapesProfile");
    private static final File repoDir = new File(testProfileDir, "repo");
    private static final File cacheDir = new File(testProfileDir, "repo");
    private static final GroovyClassLoader classLoader = new GroovyClassLoader();

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (repoDir.mkdirs()) {
            Log.info("Directory {0} was created", repoDir);
        }
        if (cacheDir.mkdirs()) {
            Log.info("Directory {0} was created", cacheDir);
        }
        //Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Test
    public void test() throws Exception {
        final Map<String, Object> args = new HashMap<>();
        args.put("classLoader", GroovyRuntime.CLASS_LOADER);
        final Map<String, Object> dependency1 = new HashMap<>();
        dependency1.put("group", "commons-cli");
        dependency1.put("module", "commons-cli");
        dependency1.put("version", "1.2");
        Grape.grab(args, dependency1);
    }
}
