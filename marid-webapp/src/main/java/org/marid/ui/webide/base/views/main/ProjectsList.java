/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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
package org.marid.ui.webide.base.views.main;

import com.vaadin.ui.Grid;
import org.marid.applib.spring.init.Init;
import org.marid.applib.spring.init.Inits;
import org.marid.ui.webide.base.MainUI;
import org.marid.ui.webide.base.dao.ProjectsDao;
import org.springframework.stereotype.Component;

@Component
public class ProjectsList extends Grid<String> implements Inits {

  @Init(1)
  public void initNameColumn(ProjectsDao dao) {
    System.out.println(1);
  }

  @Init(4)
  public void initV4(MainUI ui) {
    System.out.println(4);
  }

  @Init(3)
  public void initV3(MainUI ui) {
    System.out.println(3);
  }

  @Init(2)
  public void initV2(MainUI ui) {
    System.out.println(2);
  }
}
