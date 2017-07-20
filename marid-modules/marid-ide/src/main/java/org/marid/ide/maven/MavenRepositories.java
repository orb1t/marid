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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class MavenRepositories {

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
}
