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

import org.marid.ide.project.ProjectProfile;
import org.marid.ide.tabs.IdeTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.ide.common.IdeShapes.circle;

@Component
public class BeanTab extends IdeTab {

  @Autowired
  public BeanTab(BeanHierarchyTable table, ProjectProfile profile) {
    setContent(table);
    setText(profile.getName());
    setGraphic(circle(profile.hashCode(), 16));
  }
}
