/*-
 * #%L
 * marid-fx
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

package org.marid.jfx.props;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.misc.Calls;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public interface ObservablesProvider {

  @NotNull
  default Observable[] observables() {
    return ostream().toArray(Observable[]::new);
  }

  @NotNull
  default Stream<Observable> ostream() {
    return Stream.of(getClass().getFields())
        .filter(f -> Observable.class.isAssignableFrom(f.getType()))
        .sorted(Comparator.comparing(Field::getName))
        .map(f -> Calls.call(() -> (Observable) f.get(this)));
  }

  @NotNull
  static <T extends ObservablesProvider> FxObject<T> object() {
    return new FxObject<>(T::observables);
  }

  @NotNull
  static <T extends ObservablesProvider> FxObject<T> object(@Nullable  T value) {
    return new FxObject<>(T::observables, value);
  }

  @NotNull
  static <T extends ObservablesProvider> ObservableList<T> list() {
    return FXCollections.observableArrayList(T::observables);
  }

  @NotNull
  static <T extends ObservablesProvider> ObservableList<T> list(@NotNull List<T> list) {
    return FXCollections.observableList(list, T::observables);
  }

  @NotNull
  static <T extends ObservablesProvider> Collector<T, ?, ObservableList<T>> toObservableList() {
    return toCollection(ObservablesProvider::list);
  }
}
