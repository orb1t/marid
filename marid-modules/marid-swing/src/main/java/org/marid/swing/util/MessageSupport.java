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

package org.marid.swing.util;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MessageSupport {

    int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
    int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
    int INFO_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
    int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;
    int PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;

    default void showMessage(int messageType, String title, Object message) {
        JOptionPane.showMessageDialog(component(this), message, s(title), messageType);
    }

    default void showMessage(int messageType, String title, String message, Object... args) {
        JOptionPane.showMessageDialog(component(this), m(message, args), s(title), messageType);
    }

    default void showMessage(int messageType, String title, String message, Throwable error, Object... args) {
        final StringWriter sw = new StringWriter();
        try (final PrintWriter pw = new PrintWriter(sw)) {
            pw.println(m(message, args));
            error.printStackTrace(pw);
        }
        JOptionPane.showMessageDialog(component(this), sw.toString(), s(title), messageType);
    }

    static Component component(Object object) {
        if (object instanceof Component) {
            return (Component) object;
        } else {
            return null;
        }
    }
}
