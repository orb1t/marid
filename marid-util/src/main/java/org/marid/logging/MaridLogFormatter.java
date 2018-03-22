/*-
 * #%L
 * marid-util
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

package org.marid.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class MaridLogFormatter extends Formatter {

  @Override
  public String format(LogRecord record) {
    final StringWriter buf = new StringWriter(64);

    try (final PrintWriter writer = new PrintWriter(buf)) {
      writer.print(record.getInstant());
      writer.write(' ');

      final String levelName = record.getLevel().getName();
      writer.write(levelName.charAt(0));
      writer.write(levelName.charAt(levelName.length() - 1));

      writer.format(" %08X ", record.getThreadID());
      writer.write(record.getLoggerName());
      writer.write(' ');

      writer.println(formatMessage(record));

      if (record.getThrown() != null) {
        record.getThrown().printStackTrace(writer);
      }
    }

    return buf.toString();
  }
}
