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

package org.marid.site.util;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.sql.Date;
import java.text.DateFormat;

/**
 * @author Dmitry Ovchinnikov
 */
public interface SwtSupport extends NlsSupport {

    default void addTextRow(Table table, String label, String key, JsonObject object, boolean localizeValue) {
        final TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, s(label));
        final String value = object.get(key).asString();
        item.setText(1, localizeValue ? s(value) : value);
    }

    default void addCheckRow(Table table, String label, String key, JsonObject object) {
        final TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, s(label));
        final boolean value = object.get(key).asBoolean();
        item.setText(1, value ? "+" : "-");
    }

    default void addDateRow(Table table, String label, String key, JsonObject object) {
        final TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, s(label));
        final String value = object.get(key).asString();
        final Date date = Date.valueOf(value);
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, getDefaultL10nLocale());
        item.setText(1, dateFormat.format(date));
    }

    default void addTitle(Composite composite, String title) {
        final CLabel label = new CLabel(composite, SWT.FILL | SWT.BORDER | SWT.SHADOW_OUT);
        label.setMargins(10, 10, 10, 10);
        label.setText(s(title));
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setBackground(new Color[] {
                new Color(composite.getDisplay(), 0x40, 0x40, 0xA9),
                new Color(composite.getDisplay(), 0x60, 0x60, 0xAF),
                new Color(composite.getDisplay(), 0x60, 0x60, 0xFF)
        }, new int[] {30, 100});
        label.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        label.setFont(FontUtil.overrideFont(label.getFont(), -1, SWT.BOLD));
    }

    default Point centerPoint(Dialog dialog, Shell shell) {
        final Rectangle bounds = dialog.getParent().getClientArea(), rect = shell.getBounds();
        return new Point(bounds.x + (bounds.width - rect.width) / 2, bounds.y + (bounds.height - rect.height) / 2);
    }
}
