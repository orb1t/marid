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

import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.logging.LogSupport;
import org.marid.test.ManualTests;

import java.io.File;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({ManualTests.class})
public class IvyTest implements LogSupport {

    private static final File maridDir = new File(System.getProperty("user.home"), "marid");
    private static final File profilesDir = new File(maridDir, "profiles");
    private static final File testProfileDir = new File(profilesDir, "testProfile");
    private static final File repoDir = new File(testProfileDir, "repo");

    private static Ivy ivy;

    private static IBiblioResolver resolver(IvySettings settings) {
        final IBiblioResolver resolver = new IBiblioResolver();
        resolver.setName("public");
        resolver.setUsepoms(true);
        resolver.setUseMavenMetadata(true);
        resolver.setM2compatible(true);
        resolver.setSettings(settings);
        return resolver;
    }

    private ResolveOptions resolveOptions() {
        final ResolveOptions resolveOptions = new ResolveOptions();
        resolveOptions.setUseCacheOnly(false);
        resolveOptions.setTransitive(true);
        resolveOptions.setCheckIfChanged(true);
        resolveOptions.setRefresh(true);
        resolveOptions.setOutputReport(true);
        return resolveOptions;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (repoDir.mkdirs()) {
            Log.info("Directory {0} was created", repoDir);
        }
        final IvySettings ivySettings = new IvySettings();
        ivySettings.setBaseDir(new File(testProfileDir, "ivy"));
        ivy = Ivy.newInstance(ivySettings);
        ivy.configure(IvyTest.class.getResource("ivy-settings.xml"));
        ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_DEBUG));
    }

    @Test
    public void download() throws Exception {
        ivy.execute((livy, context) -> {
            try {
                final ResolveReport resolveReport = livy.resolve(getClass().getResource("ivy-module.xml"));
                for (final Object message : resolveReport.getAllProblemMessages()) {
                    System.out.println(message);
                }
                return null;
            } catch (Exception x) {
                throw new IllegalStateException(x);
            }
        });
    }
}
