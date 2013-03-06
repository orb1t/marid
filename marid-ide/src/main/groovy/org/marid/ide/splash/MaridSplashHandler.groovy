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

package org.marid.ide.splash

import java.util.logging.Handler
import java.util.logging.LogRecord

/**
 * Marid splash handler.
 *
 * @author Dmitry Ovchinnikov 
 */
class MaridSplashHandler extends Handler {

    private final MaridSplash splash;

    MaridSplashHandler(MaridSplash splash) {
        this.splash = splash;
    }

    @Override
    void publish(LogRecord record) {

    }

    @Override
    void flush() {

    }

    @Override
    void close() throws SecurityException {

    }
}
