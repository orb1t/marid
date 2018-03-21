/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.app.logging;

import java.io.PrintStream;
import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class MaridLogHandler extends Handler {

  private static final int WARN_LEVEL = Level.WARNING.intValue();

  public MaridLogHandler() {
    setFormatter(new SimpleFormatter());
  }

  @Override
  public void publish(LogRecord record) {
    final String instant = Instant.ofEpochMilli(record.getMillis()).toString();
    final String message = getFormatter().formatMessage(record);

    print(System.out, record, instant, message);

    if (record.getLevel().intValue() > WARN_LEVEL) {
      print(System.err, record, instant, message);
    }
  }

  private void print(PrintStream printStream, LogRecord record, String instant, String message) {
    final String levelName = record.getLevel().getName();
    final String text = instant
        + ' '
        + levelName.charAt(0)
        + levelName.charAt(levelName.length() - 1)
        + ' '
        + record.getLoggerName()
        + ' '
        + message;
    if (record.getThrown() == null) {
      printStream.println(text);
    } else {
      synchronized ((Object) printStream) {
        printStream.println(text);
        record.getThrown().printStackTrace(printStream);
      }
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() {
  }
}
