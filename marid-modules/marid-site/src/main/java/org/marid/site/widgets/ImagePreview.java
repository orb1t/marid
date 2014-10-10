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

package org.marid.site.widgets;

import org.eclipse.rap.rwt.widgets.DialogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.marid.site.images.ImageUtil;
import org.marid.site.util.SwtSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class ImagePreview extends Button implements SelectionListener, SwtSupport {

    private final Image fullImage;

    public ImagePreview(Composite parent, int style, Image fullImage, Image previewImage) {
        super(parent, style);
        this.fullImage = fullImage;
        setImage(previewImage);
        addSelectionListener(this);
    }

    public Image getFullImage() {
        return fullImage;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        final Dialog dialog = new Dialog(getShell(), SWT.TITLE | SWT.CLOSE | SWT.BORDER) {
            @Override
            protected void prepareOpen() {
                shell = new Shell(getParent(), getStyle());
                shell.setLayout(new FillLayout());
                final Label label = new Label(shell, SWT.NONE);
                final Rectangle r = getParent().getClientArea();
                final Rectangle bounds = new Rectangle(0, 0, r.width - 10, r.height - 50);
                label.setImage(ImageUtil.fit(fullImage, bounds));
                shell.pack();
                shell.setLocation(centerPoint(this, shell));
            }
        };
        DialogUtil.open(dialog, event -> {});
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {

    }
}
