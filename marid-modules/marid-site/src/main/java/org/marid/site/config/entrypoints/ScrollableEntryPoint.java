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

package org.marid.site.config.entrypoints;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class ScrollableEntryPoint extends AbstractEntryPoint {

    @Override
    protected void createContents(Composite parent) {
        parent.setLayout(new FillLayout());
        final ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL);
        final Composite composite = new Composite(sc, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        fillContents(composite);
        sc.setContent(composite);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        sc.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                final Rectangle rectangle = sc.getClientArea();
                sc.setMinSize(composite.computeSize(rectangle.width, SWT.DEFAULT));
            }
        });
    }

    protected abstract void fillContents(Composite composite);
}
