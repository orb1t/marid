/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.dependant.beaneditor;

import org.marid.idefx.beans.IdeBean;
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
