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

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BugTab implements Tab {
    @Override
    public void fill(Composite composite) {
        final Browser browser = new Browser(composite, SWT.FILL);
        browser.setLayoutData(new GridData(GridData.FILL_BOTH));
        browser.setUrl("http://savannah.nongnu.org/projects/libcgc/");
    }

    @Override
    public String getName() {
        return "/bug";
    }

    @Override
    public String getTitle() {
        return "Bug tracking";
    }

    @Override
    public String getIcon() {
        return "images/bug.png";
    }

    @Override
    public int getOrder() {
        return 40;
    }
}
