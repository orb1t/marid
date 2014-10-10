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

package org.marid.site.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.marid.site.util.FontUtil;
import org.marid.site.util.NlsSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridVideoDialog extends Dialog implements NlsSupport {

    private final String url;

    public MaridVideoDialog(Shell parent, String url) {
        super(parent, SWT.MODELESS);
        this.url = url;
    }

    private void createControls() {
        final Label videoLabel = new Label(shell, SWT.NONE);
        videoLabel.setFont(FontUtil.overrideFont(videoLabel.getFont(), -1, SWT.BOLD));
        videoLabel.setText(s("Video sample") + ":");
        final Browser browser = new Browser(shell, SWT.NONE);
        browser.setLayoutData(new GridData(600, 600));
        browser.setUrl(url);
    }

    protected Point centerPoint() {
        final Rectangle bounds = getParent().getBounds(), rect = shell.getBounds();
        return new Point(bounds.x + (bounds.width - rect.width) / 2, bounds.y + (bounds.height - rect.height) / 2);
    }

    @Override
    protected void prepareOpen() {
        super.prepareOpen();
        shell = new Shell(getParent(), SWT.TITLE | SWT.BORDER);
        shell.setLayout(new GridLayout(1, false));
        createControls();
        shell.pack();
        shell.setLocation(centerPoint());
    }
}
