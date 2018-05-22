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

import com.vaadin.data.provider.ListDataProvider;
import org.marid.ui.webide.base.dao.ProjectsDao;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Component
public class MainViewModel {

  private final ProjectsDao dao;
  private final ArrayList<Project> projects = new ArrayList<>();
  private final ListDataProvider<Project> dataProvider = new ListDataProvider<>(projects);

  public MainViewModel(ProjectsDao dao) {
    this.dao = dao;
  }

  @PostConstruct
  public void refresh() {
    projects.clear();
    dao.getProjectNames().stream().map(Project::new).forEach(projects::add);
    dataProvider.refreshAll();
  }

  public ListDataProvider<Project> getDataProvider() {
    return dataProvider;
  }

  public class Project {

    private final String name;
    private long size;

    private Project(String name) {
      this.name = name;
      this.size = dao.getSize(name);
    }

    public String getName() {
      return name;
    }

    public long getSize() {
      return size;
    }

    public void refresh() {
      size = dao.getSize(name);
      dataProvider.refreshItem(this);
    }
  }
}
