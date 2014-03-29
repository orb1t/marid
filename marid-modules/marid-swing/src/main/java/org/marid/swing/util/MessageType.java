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

/**
 * @author Dmitry Ovchinnikov.
 */
public enum MessageType {

    INFO(JOptionPane.INFORMATION_MESSAGE),
    WARNING(JOptionPane.WARNING_MESSAGE),
    ERROR(JOptionPane.ERROR_MESSAGE),
    QUESTION(JOptionPane.QUESTION_MESSAGE),
    PLAIN(JOptionPane.PLAIN_MESSAGE);

    public final int messageType;

    private MessageType(int messageType) {
        this.messageType = messageType;
    }
}
