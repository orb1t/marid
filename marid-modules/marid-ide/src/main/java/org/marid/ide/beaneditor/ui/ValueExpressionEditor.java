/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.beaneditor.ui;

import javafx.beans.property.StringProperty;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.marid.jfx.ScrollPanes;
import org.marid.l10n.L10nSupport;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueExpressionEditor extends BorderPane implements L10nSupport {

    private final TextArea textArea;

    public ValueExpressionEditor(StringProperty value) {
        final String text = value.get() == null ? "" : value.get();
        this.textArea = new TextArea(text);
        if (text.startsWith("#{")) {
            textArea.setText(text.substring(2, text.length() - (text.endsWith("}") ? 1 : 0)));
        }
        setCenter(ScrollPanes.scrollPane(textArea));
    }

    public String accept() {
        return "#{" + textArea.getText() + "}";
    }
}
