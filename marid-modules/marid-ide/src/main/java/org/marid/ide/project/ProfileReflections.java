/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.project;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.marid.spring.xml.AbstractData;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.marid.misc.Iterables.stream;
import static org.springframework.util.ReflectionUtils.doWithFields;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ProfileReflections {

    public static Stream<Pair<AbstractData<?>, Observable[]>> observableStream(ProjectProfile profile) {
        return profile.getBeanFiles().stream()
                .flatMap(f -> dataStream(f.stream()))
                .map(d -> {
                    final Observable[] observables = observableStream(of(d.observables())).toArray(Observable[]::new);
                    return new Pair<>(d, observables);
                });
    }

    private static Stream<AbstractData<?>> dataStream(Stream<? extends AbstractData<?>> stream) {
        return stream.flatMap(d -> concat(of(d), dataStream(d.stream())));
    }

    public static Stream<Observable> observableStream(Stream<Observable> stream) {
        return stream.flatMap(o -> {
            if (o instanceof ObservableList) {
                final ObservableList<?> list = (ObservableList<?>) o;
                return concat(of(o), observableStream(stream(Observable.class, list.stream())));
            } else if (o instanceof ObservableValue) {
                final ObservableValue<?> value = (ObservableValue<?>) o;
                final Object v = value.getValue();
                if (v instanceof Observable) {
                    return concat(of(o), observableStream(of((Observable) v)));
                } else {
                    return of(o);
                }
            } else {
                return of(o);
            }
        });
    }

    public static int countListeners(Observable observable) {
        final AtomicInteger count = new AtomicInteger();
        doWithFields(observable.getClass(), field -> {
            field.setAccessible(true);
            final Object helper = field.get(observable);
            if (helper == null) {
                return;
            }
            doWithFields(helper.getClass(), f -> {
                if (f.getName().equals("listener")) {
                    f.setAccessible(true);
                    final Object listener = f.get(helper);
                    if (listener != null) {
                        count.incrementAndGet();
                    }
                } else if (f.getName().equals("invalidationSize") || f.getName().equals("changeSize")) {
                    f.setAccessible(true);
                    count.addAndGet(f.getInt(helper));
                }
            });
        }, field -> field.getName().toLowerCase().endsWith("helper"));
        return count.get();
    }
}
