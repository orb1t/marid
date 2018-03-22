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

import java.time.Duration;
import java.util.logging.*;

public abstract class AbstractHandler extends Handler {

  public AbstractHandler(Formatter defaultFormatter, Filter defaultFilter) {
    setFormatter(formatter(LogManager.getLogManager(), "formatter", defaultFormatter));
    setFilter(filter(LogManager.getLogManager(), "filter", defaultFilter));
  }

  public AbstractHandler() {
    this(new MaridLogFormatter(), null);
  }

  protected Formatter formatter(LogManager logManager, String key, Formatter defaultFormatter) {
    final String formatter = logManager.getProperty(getClass().getName() + "." + key);
    if (formatter == null) {
      return defaultFormatter;
    } else {
      try {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (Formatter) classLoader.loadClass(formatter).getConstructor().newInstance();
      } catch (Exception x) {
        reportError("Unable to initialize " + key + " " + formatter, x, ErrorManager.OPEN_FAILURE);
        return defaultFormatter;
      }
    }
  }

  protected Filter filter(LogManager logManager, String key, Filter defaultFilter) {
    final String filter = logManager.getProperty(getClass().getName() + "." + key);
    if (filter == null) {
      return defaultFilter;
    } else {
      try {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (Filter) classLoader.loadClass(filter).getConstructor().newInstance();
      } catch (Exception x) {
        reportError("Unable to initialize " + key + " " + filter, x, ErrorManager.OPEN_FAILURE);
        return defaultFilter;
      }
    }
  }

  protected Duration duration(LogManager logManager, String key, Duration defaultDuration) {
    final String duration = logManager.getProperty(getClass().getName() + "." + key);
    if (duration == null) {
      return defaultDuration;
    } else {
      try {
        return Duration.parse(duration);
      } catch (Exception x) {
        reportError("Unable to initialize " + key + " " + duration, x, ErrorManager.OPEN_FAILURE);
        return defaultDuration;
      }
    }
  }

  protected int intValue(LogManager logManager, String key, int defaultValue) {
    final String v = logManager.getProperty(getClass().getName() + "." + key);
    if (v == null) {
      return defaultValue;
    } else {
      try {
        return Integer.parseInt(v);
      } catch (Exception x) {
        reportError("Unable to initialize " + key + " " + v, x, ErrorManager.OPEN_FAILURE);
        return defaultValue;
      }
    }
  }

  protected String string(LogManager logManager, String key, String defaultValue) {
    final String v = logManager.getProperty(key);
    return v == null ? defaultValue : v;
  }
}
