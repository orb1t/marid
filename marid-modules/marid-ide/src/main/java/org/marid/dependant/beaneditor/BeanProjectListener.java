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

package org.marid.dependant.beaneditor;

import org.marid.beans.IdeBean;
import org.marid.ide.project.ProjectFileType;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.project.ProjectSaveEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

@Component
public class BeanProjectListener {

    private final ProjectProfile profile;
    private final IdeBean root;

    @Autowired
    public BeanProjectListener(ProjectProfile profile, IdeBean root) {
        this.profile = profile;
        this.root = root;
    }

    @EventListener
    public void onProjectSave(ProjectSaveEvent event) {
        final Path path = profile.get(ProjectFileType.BEANS_XML);
        root.save(path);
        log(INFO, "Saved {0}", path);
    }
}
