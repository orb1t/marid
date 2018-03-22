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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.ErrorManager;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.*;

public class MaridFileLogHandler extends AbstractHandler {

  private final int maxFiles;
  private final String pattern;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  private BufferedWriter writer;
  private int day;

  public MaridFileLogHandler() {
    maxFiles = intValue(LogManager.getLogManager(), "maxFiles", 5);
    pattern = string(LogManager.getLogManager(), "pattern", "logs/log-%s.log");
  }

  @Override
  public void publish(LogRecord record) {
    if (isLoggable(record)) {
      return;
    }

    final String message;
    try {
      message = getFormatter().format(record);
    } catch (Exception x) {
      reportError("Unable to format", x, ErrorManager.FORMAT_FAILURE);
      return;
    }

    lock.readLock().lock();
    try {
      if (writer == null || day(record) > day) {
        lock.readLock().unlock();

        lock.writeLock().lock();
        try {
          day = day(record);

          LocalDate date = record.getInstant().atZone(ZoneId.systemDefault()).toLocalDate();

          final Path path = Paths.get(String.format(pattern, date)).toAbsolutePath();
          final Path dir = path.getParent();

          try {
            Files.createDirectories(dir);

            if (writer != null) {
              writer.close();
            }

            writer = newBufferedWriter(path, UTF_8, CREATE, APPEND, WRITE);

            for (int i = 0; i < Short.MAX_VALUE; i++, date = date.minus(1L, ChronoUnit.DAYS)) {
              final Path p = Paths.get(String.format(pattern, date));
              if (!Files.isRegularFile(p)) {
                break;
              }
              if (i > maxFiles) {
                Files.deleteIfExists(p);
              }
            }
          } catch (Exception x) {
            reportError("Unable to write", x, ErrorManager.WRITE_FAILURE);
          }
        } finally {
          lock.writeLock().unlock();
        }

        lock.readLock().lock();
      }

      try {
        writer.write(message);
        writer.flush();
      } catch (Exception x) {
        reportError("Unable to write", x, ErrorManager.WRITE_FAILURE);
      }
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws SecurityException {
    lock.readLock().lock();
    try {
      writer.close();
    } catch (IOException x) {
      throw new SecurityException(x);
    } finally {
      lock.readLock().unlock();
    }
  }

  private int day(LogRecord record) {
    return record.getInstant().atZone(ZoneId.systemDefault()).getDayOfYear();
  }
}
