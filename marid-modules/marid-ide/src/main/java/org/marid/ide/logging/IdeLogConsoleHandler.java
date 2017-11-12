/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.logging;

import org.marid.io.AppendableWriter;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeLogConsoleHandler extends Handler {

  @Override
  public void publish(LogRecord record) {
    if (isLoggable(record)) {
      final StringBuffer builder = new StringBuffer();
      try (final Formatter formatter = new Formatter(builder)) {
        formatter.format("%tF %tT ", record.getMillis(), record.getMillis());
        formatter.flush();
        builder.append(level(record.getLevel()));
        builder.append(' ');
        builder.append(abbreviate(record.getLoggerName()));
        builder.append(' ');
        m(Locale.getDefault(), record.getMessage(), builder, record.getParameters());
        builder.append(System.lineSeparator());
        if (record.getThrown() != null) {
          try (final PrintWriter printWriter = new PrintWriter(new AppendableWriter(builder))) {
            record.getThrown().printStackTrace(printWriter);
          }
        }
      }
      System.out.append(builder);
    }
  }

  @Override
  public void flush() {
    System.out.flush();
  }

  @Override
  public void close() throws SecurityException {
  }

  private static char level(Level level) {
    switch (level.intValue()) {
      case 800: return 'I';
      case 900: return 'W';
      case 1000: return 'E';
      case 300: return 'T';
      case 400: return 'F';
      case 500: return 'D';
      case 700: return 'C';
      default: return '*';
    }
  }

  private static char[] abbreviate(String logger) {
    final char[] result = new char[64];
    if (logger != null) {
      final int pos = logger.lastIndexOf('.');
      final int offset = pos >= 0 ? pos + 1 : 0;
      final int len = Math.min(logger.length() - offset, result.length);
      for (int i = 0; i < len; i++) {
        result[i] = logger.charAt(i + offset);
      }
      Arrays.fill(result, len, result.length, ' ');
    } else {
      Arrays.fill(result, ' ');
    }
    return result;
  }
}
