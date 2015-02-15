/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.swing.adapters;

import org.marid.swing.SwingUtil;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.io.IOException;
import java.io.Writer;

/**
 * @author Dmitry Ovchinnikov
 */
public class TextAreaWriter extends Writer {

    private final JTextArea textArea;

    public TextAreaWriter(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(@Nonnull char[] cbuf, int off, int len) throws IOException {
        SwingUtil.execute(() -> textArea.append(new String(cbuf, off, len)));
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    public void reset() {
        SwingUtil.execute(() -> textArea.setText(""));
    }
}
