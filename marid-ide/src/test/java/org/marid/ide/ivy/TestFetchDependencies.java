/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.plugins.resolver.URLResolver;
import org.apache.ivy.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;

/**
 * @author Dmitry Ovchinnikov
 */
public class TestFetchDependencies {

    private final IvySettings settings = new IvySettings();
    private Ivy ivy;
    private File baseDir;

    @Before
    public void init() throws IOException {
        baseDir = Files.createTempDirectory("fetch").toFile();
        settings.setBaseDir(baseDir);
        IBiblioResolver resolver = new IBiblioResolver();
        //resolver.addArtifactPattern("http://repo1.maven.org/maven2/[organisation]/[module]/[revision]/[artifact](-[revision]).[ext]");
        resolver.setName("resolver1");
        resolver.setM2compatible(true);
        settings.addResolver(resolver);
        settings.setDefaultResolver(resolver.getName());
        ivy = Ivy.newInstance(settings);
    }

    @Test
    public void resolveTest() throws Exception {
        ResolveOptions ro = new ResolveOptions();
        ro.setConfs(new String[] {"default"});
        ModuleRevisionId id = ModuleRevisionId.newInstance("org.codehaus.groovy", "groovy-all", "2.1.4");
        ResolveReport report = ivy.resolve(id, ro, true);
        if (report.hasError()) {
            for (Object msg : report.getAllProblemMessages()) {
                System.err.println(msg);
            }
        }
        RetrieveOptions reo = new RetrieveOptions();
        reo.setConfs(new String[] {"default"});
        reo.setOverwriteMode(RetrieveOptions.OVERWRITEMODE_DIFFERENT);
        assertFalse(report.hasError());
    }

    @After
    public void close() throws IOException {
        FileUtil.forceDelete(baseDir);
    }
}
