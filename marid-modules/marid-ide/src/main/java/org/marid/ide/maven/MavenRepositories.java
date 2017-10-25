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

package org.marid.ide.maven;

import org.marid.ide.common.Directories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class MavenRepositories {

    private static final String MAVEN_URL = "http://www.apache.org/dist/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.zip";

    private final Path baseDir;
    private final File centralDir;
    private final List<MavenRepository> repositories = new ArrayList<>();

    @Autowired
    public MavenRepositories(Directories directories) {
        baseDir = directories.getMarid().resolve("cache").resolve("repos");
        centralDir = baseDir.resolve("central").resolve("cache").toFile();

        // maven central
        repositories.add(new MavenRepository("central", centralDir, "http://repo1.maven.org/maven2"));

        // local repository
        repositories.add(new MavenRepository("local", directories.getRepo().toFile(), null));
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public List<MavenRepository> getRepositories() {
        return repositories;
    }

    @Autowired
    private void initMaven(Directories directories) {
        final Path mavenHome = directories.getMaven();
        final Pattern slashPattern = Pattern.compile("/");

        try {
            if (Files.notExists(mavenHome) || Files.list(mavenHome).count() == 0) {
                final URL mavenZip = new URL(MAVEN_URL);
                try (final ZipInputStream zis = new ZipInputStream(mavenZip.openStream())) {
                    for (ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry()) {
                        if (e.isDirectory()) {
                            continue;
                        }
                        try {
                            final Path path = slashPattern.splitAsStream(e.getName())
                                    .skip(1L)
                                    .reduce(mavenHome, Path::resolve, (p1, p2) -> p2);
                            final Path dir = path.getParent();
                            Files.createDirectories(dir);
                            Files.copy(zis, path, REPLACE_EXISTING);
                            log(INFO, "Copied {0} to {1}", e.getName(), path);
                        } finally {
                            zis.closeEntry();
                        }
                    }
                }
            }
        } catch (Exception x) {
            log(WARNING, "Unable to initialize maven", x);
        }
    }
}
