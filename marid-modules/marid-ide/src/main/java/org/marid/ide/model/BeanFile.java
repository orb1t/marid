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

package org.marid.ide.model;

import org.marid.ide.project.ProjectProfile;
import org.marid.io.Xmls;
import org.marid.runtime.beans.Bean;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.project.ProjectFileType.BEANS_XML;
import static org.marid.io.Xmls.read;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFile extends BeanData {

    public BeanFile(ProjectProfile profile) {
        final Path file = profile.get(BEANS_XML);
        if (Files.notExists(file)) {
            save(profile);
        } else {
            read(file, Bean::new).children.forEach(this::add);
        }
    }

    public void save(ProjectProfile profile) {
        try {
            Xmls.writeFormatted("beans", e -> toBean().writeTo(e), profile.get(BEANS_XML));
        } catch (Exception x) {
            log(WARNING, "Unable to save {0}", x, profile.get(BEANS_XML));
        }
    }

    @Override
    public String toString() {
        return "";
    }
}
