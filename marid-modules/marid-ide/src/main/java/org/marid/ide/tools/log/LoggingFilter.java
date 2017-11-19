/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
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

package org.marid.ide.tools.log;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.marid.function.ForwardedPredicate;
import org.marid.idelib.spring.ui.FxComponent;

import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@FxComponent
public class LoggingFilter implements Predicate<LogRecord> {

  private final BooleanProperty offProperty = new SimpleBooleanProperty();
  private final BooleanProperty allProperty = new SimpleBooleanProperty(true);
  private final Map<Level, BooleanProperty> properties = Stream.of(
      Level.SEVERE,
      Level.WARNING,
      Level.INFO,
      Level.CONFIG,
      Level.FINE,
      Level.FINER,
      Level.FINEST
  ).collect(Collectors.toMap(e -> e, e -> new SimpleBooleanProperty(Level.ALL.equals(e))));
  private FilteredList<LogRecord> filteredList;

  public LoggingFilter() {
    offProperty.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        properties.forEach((l, p) -> p.setValue(false));
        allProperty.set(false);
      }
      filteredList.setPredicate(new ForwardedPredicate<>(this));
    });
    allProperty.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        properties.forEach((l, p) -> p.setValue(false));
        offProperty.set(false);
        filteredList.setPredicate(null);
      } else {
        filteredList.setPredicate(new ForwardedPredicate<>(this));
      }
    });
    properties.forEach((l, p) -> p.addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        allProperty.set(false);
        offProperty.set(false);
      }
      filteredList.setPredicate(new ForwardedPredicate<>(this));
    }));
  }

  public BooleanProperty getProperty(Level level) {
    switch (level.intValue()) {
      case Integer.MAX_VALUE:
        return offProperty;
      case Integer.MIN_VALUE:
        return allProperty;
      default:
        return properties.get(level);
    }
  }

  public void clear() {
    filteredList.getSource().clear();
  }

  public FilteredList<LogRecord> filteredList(ObservableList<LogRecord> list) {
    return filteredList = new FilteredList<>(list, this);
  }

  @Override
  public boolean test(LogRecord logRecord) {
    if (allProperty.get()) {
      return true;
    } else if (offProperty.get()) {
      return false;
    } else {
      final BooleanProperty property = properties.get(logRecord.getLevel());
      return property != null && property.get();
    }
  }
}
