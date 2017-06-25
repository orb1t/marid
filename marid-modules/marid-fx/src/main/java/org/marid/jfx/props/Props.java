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

import javafx.beans.property.*;
import javafx.beans.value.WritableObjectValue;
import javafx.event.Event;
import javafx.event.EventHandler;

import javax.annotation.Nonnull;
import java.util.function.*;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Props {

    static StringProperty stringProp(Supplier<String> supplier, Consumer<String> consumer) {
        final StringProperty property = new SimpleStringProperty(supplier.get());
        property.addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
        return property;
    }

    static BooleanProperty boolProp(BooleanSupplier supplier, Consumer<Boolean> consumer) {
        final BooleanProperty property = new SimpleBooleanProperty(supplier.getAsBoolean());
        property.addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
        return property;
    }

    static IntegerProperty intProp(IntSupplier supplier, IntConsumer consumer) {
        final IntegerProperty property = new SimpleIntegerProperty(supplier.getAsInt());
        property.addListener((observable, oldValue, newValue) -> consumer.accept(newValue.intValue()));
        return property;
    }

    static <T> ObjectProperty<T> prop(Supplier<T> supplier, Consumer<T> consumer) {
        final ObjectProperty<T> property = new SimpleObjectProperty<>(supplier.get());
        property.addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
        return property;
    }

    static <T> WritableObjectValue<T> value(Supplier<T> supplier, Consumer<T> consumer) {
        return new WritableValueImpl<>(consumer, supplier);
    }

    static WritableObjectValue<String> string(@Nonnull Preferences node, @Nonnull String key, String defaultValue) {
        return value(() -> node.get(key, defaultValue), v -> {
            if (v == null || v.isEmpty()) {
                node.remove(key);
            } else {
                node.put(key, v);
            }
        });
    }

    static <E extends Event> void addHandler(Property<EventHandler<E>> property, EventHandler<E> handler) {
        final EventHandler<E> old = property.getValue();
        if (old == null) {
            property.setValue(handler);
        } else {
            property.setValue(event -> {
                old.handle(event);
                handler.handle(event);
            });
        }
    }
}
