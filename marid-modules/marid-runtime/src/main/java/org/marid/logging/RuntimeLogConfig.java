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

import org.marid.logging.formatters.DefaultFormatter;

import java.lang.reflect.Field;
import java.util.logging.FileHandler;

/**
 * @author Dmitry Ovchinnikov.
 */
public class RuntimeLogConfig {

    public RuntimeLogConfig() throws Exception {
        final FileHandler fileHandler = new FileHandler("logs/%g.log");
        fileHandler.setFormatter(new DefaultFormatter());
        final Field countField = FileHandler.class.getDeclaredField("count");
        final Field limitField = FileHandler.class.getDeclaredField("limit");
        final Field appendField = FileHandler.class.getDeclaredField("append");
        countField.setAccessible(true);
        limitField.setAccessible(true);
        appendField.setAccessible(true);
        countField.setInt(fileHandler, 10);
        limitField.setInt(fileHandler, 1_000_000);
        appendField.setBoolean(fileHandler, true);
        Logging.rootLogger().addHandler(fileHandler);
    }
}
