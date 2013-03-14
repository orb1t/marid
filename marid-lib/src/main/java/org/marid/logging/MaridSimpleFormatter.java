/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.logging;

import org.marid.l10n.Localized;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridSimpleFormatter extends SimpleFormatter implements Localized {

    @Override
    public synchronized String formatMessage(LogRecord record) {
        record.setResourceBundle(M.MB);
        return super.formatMessage(record);
    }
}
