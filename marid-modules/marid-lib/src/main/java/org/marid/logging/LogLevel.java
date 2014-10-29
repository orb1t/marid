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

package org.marid.logging;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * @author Dmitry Ovchinnikov
 */
public enum LogLevel {

    ALL(Level.ALL),
    FINEST(Level.FINEST),
    FINER(Level.FINER),
    FINE(Level.FINE),
    CONFIG(Level.CONFIG),
    INFO(Level.INFO),
    WARNING(Level.WARNING),
    SEVERE(Level.SEVERE),
    OFF(Level.OFF);

    private final Level level;

    private LogLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    private static final LogLevel[] LOG_LEVELS = LogLevel.values();
    private static final int[] LEVELS = Arrays.stream(LOG_LEVELS).mapToInt(l -> l.getLevel().intValue()).toArray();

    public static LogLevel findBy(Level level) {
        final int index = Arrays.binarySearch(LEVELS, level.intValue());
        return index >= 0 ? LOG_LEVELS[index] : OFF;
    }
}
