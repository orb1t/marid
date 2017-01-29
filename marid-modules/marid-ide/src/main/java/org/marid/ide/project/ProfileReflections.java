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
import javafx.beans.WeakListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.marid.spring.xml.AbstractData;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
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
                .flatMap(f -> Stream.concat(Stream.of(f), dataStream(f.stream())))
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

    public static List<Object> listeners(Observable observable) {
        final List<Object> listeners = new ArrayList<>();
        doWithFields(observable.getClass(), field -> {
            field.setAccessible(true);
            final Object helper = field.get(observable);
            if (helper == null) {
                return;
            }
            final Consumer<Object[]> add = v -> of(v).filter(Objects::nonNull).forEach(listeners::add);
            doWithFields(helper.getClass(), f -> {
                switch (f.getName()) {
                    case "invalidationListeners":
                    case "changeListeners":
                        f.setAccessible(true);
                        ofNullable(f.get(helper)).ifPresent(v -> add.accept((Object[]) v));
                        break;
                    case "listener": {
                        f.setAccessible(true);
                        add.accept(new Object[]{f.get(helper)});
                        break;
                    }
                }
            });
        }, field -> field.getName().toLowerCase().endsWith("helper"));
        return listeners;
    }

    public static int collect(Observable observable, List<Object> listeners) {
        final AtomicInteger count = new AtomicInteger();
        for (final Object listener : listeners) {
            final AtomicBoolean remove = new AtomicBoolean();
            if (listener instanceof WeakListener) {
                final WeakListener weak = (WeakListener) listener;
                remove.set(weak.wasGarbageCollected());
            } else {
                doWithFields(listener.getClass(), f -> {
                    f.setAccessible(true);
                    final WeakReference ref = (WeakReference) f.get(listener);
                    remove.set(ref.get() == null);
                }, f -> WeakReference.class.isAssignableFrom(f.getType()));
            }
            if (remove.get()) {
                for (final Method method : observable.getClass().getMethods()) {
                    if (!method.getName().equals("removeListener")) {
                        continue;
                    }
                    if (method.getParameterCount() != 1) {
                        continue;
                    }
                    if (!method.getParameterTypes()[0].isAssignableFrom(listener.getClass())) {
                        continue;
                    }
                    try {
                        method.invoke(observable, listener);
                        count.incrementAndGet();
                    } catch (ReflectiveOperationException x) {
                        throw new IllegalStateException(x);
                    }
                }
            }
        }
        return count.get();
    }
}
