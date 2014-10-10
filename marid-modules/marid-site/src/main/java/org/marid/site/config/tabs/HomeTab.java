/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.site.config.tabs;

import org.eclipse.swt.widgets.Composite;
import org.marid.logging.LogSupport;
import org.marid.site.config.ProjectSummary;
import org.marid.site.util.NlsSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class HomeTab implements Tab, LogSupport, NlsSupport {

    private final ProjectSummary projectSummary;

    @Autowired
    public HomeTab(ProjectSummary projectSummary) {
        this.projectSummary = projectSummary;
    }

    @Override
    public void fill(Composite composite) {
        projectSummary.fill(composite);
    }

    @Override
    public String getName() {
        return "/home";
    }

    @Override
    public String getTitle() {
        return "Home";
    }

    @Override
    public String getIcon() {
        return "images/home.png";
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
