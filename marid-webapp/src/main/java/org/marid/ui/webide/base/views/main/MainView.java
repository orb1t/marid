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

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import org.marid.applib.l10n.Strs;
import org.marid.applib.spring.init.Init;
import org.marid.applib.spring.init.Inits;
import org.marid.applib.view.StaticView;
import org.marid.applib.view.ViewName;
import org.marid.misc.StringUtils;
import org.marid.ui.webide.base.dao.ProjectsDao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@ViewName("")
@Component
public class MainView extends VerticalLayout implements StaticView, Inits {

  private static final int COLUMN_GROUP = 1;

  private final ProjectsDao dao;
  private final Grid<String> grid;
  private final List<String> projects;
  private final ListDataProvider<String> dataProvider;

  public MainView(ProjectsDao dao, MainMenuBar menuBar) {
    this.dao = dao;

    grid = new Grid<>(dataProvider = new ListDataProvider<>(projects = dao.getProjectNames()));
    grid.setWidth(100, Unit.PERCENTAGE);

    addComponent(menuBar);
    addComponentsAndExpand(grid);
  }

  @Init(group = COLUMN_GROUP, value = 1)
  public void initNameColumn(Strs strs) {
    final var col = grid.addColumn(ValueProvider.identity());
    col.setCaption(strs.s("name"));
    col.setId("name");
    col.setExpandRatio(4);
  }

  @Init(group = COLUMN_GROUP, value = 2)
  public void initSizeColumn(Strs strs, Locale locale) {
    final var col = grid.addColumn(name -> StringUtils.sizeBinary(locale, dao.getSize(name), 3));
    col.setCaption(strs.s("size"));
    col.setId("size");
    col.setExpandRatio(1);
    col.setMinimumWidthFromContent(true);
  }
}
