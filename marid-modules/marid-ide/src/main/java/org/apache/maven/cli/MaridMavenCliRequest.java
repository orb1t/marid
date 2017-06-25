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

package org.apache.maven.cli;

import org.codehaus.plexus.classworlds.ClassWorld;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridMavenCliRequest extends CliRequest {

    public MaridMavenCliRequest(String[] args, ClassWorld classWorld) {
        super(args, classWorld);
    }

    public MaridMavenCliRequest directory(Path directory) {
        workingDirectory = directory.toAbsolutePath().toString();
        multiModuleProjectDirectory = directory.toFile();
        return this;
    }
}
