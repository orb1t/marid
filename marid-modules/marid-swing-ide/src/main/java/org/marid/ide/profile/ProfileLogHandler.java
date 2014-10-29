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

package org.marid.ide.profile;

import org.marid.logging.Logging;

import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProfileLogHandler extends Handler {

    protected final String prefix;
    protected final Handler delegate;

    public ProfileLogHandler(Profile profile, Handler delegate) {
        this.prefix = Objects.requireNonNull(profile.getName());
        this.delegate = delegate;
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            delegate.publish(record);
        }
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public void close() throws SecurityException {
        delegate.close();
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return record.getLoggerName() != null && prefix.equals(Logging.getPrefix(record.getLoggerName()));
    }
}
