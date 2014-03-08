/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.management;

import javax.management.*;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.util.Collections.singleton;
import static java.util.Collections.synchronizedMap;
import static org.marid.management.MaridNotificationEmitter.Emitter.ListenerInfo;
import static org.marid.management.MaridNotificationEmitter.Emitter.WildcardListenerInfo;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MaridNotificationEmitter extends NotificationEmitter {

    @Override
    default void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        final Emitter emitter = Objects.requireNonNull(Emitter.EMITTER_MAP.get(this), "No such emitter");
        final boolean removed = emitter.listeners.removeAll(singleton(new WildcardListenerInfo(listener)));
        if (!removed) {
            throw new ListenerNotFoundException("Listener not registered");
        }
    }

    @Override
    default void removeNotificationListener(NotificationListener listener,
                                            NotificationFilter filter,
                                            Object handback) throws ListenerNotFoundException {
        final Emitter emitter = Objects.requireNonNull(Emitter.EMITTER_MAP.get(this), "No such emitter");
        final boolean removed = emitter.listeners.removeAll(singleton(new ListenerInfo(listener, filter, handback)));
        if (!removed) {
            throw new ListenerNotFoundException("Listener not registered");
        }
    }

    @Override
    default void addNotificationListener(NotificationListener listener,
                                         NotificationFilter filter,
                                         Object handback) throws IllegalArgumentException {
        final Emitter emitter = Emitter.EMITTER_MAP.computeIfAbsent(this, e -> new Emitter());
        final boolean added = emitter.listeners.add(new ListenerInfo(listener, filter, handback));
        if (!added) {
            throw new IllegalArgumentException("Unable to add listener");
        }
    }

    class Emitter {

        protected static Map<MaridNotificationEmitter, Emitter> EMITTER_MAP = synchronizedMap(new WeakHashMap<>());

        protected final Queue<ListenerInfo> listeners = new ConcurrentLinkedQueue<>();

        public static void sendNotification(MaridNotificationEmitter emitter, Notification notification, Executor executor, Consumer<Exception> exceptionConsumer) {
            if (notification == null) {
                return;
            }
            for (final ListenerInfo listener : EMITTER_MAP.computeIfAbsent(emitter, e -> new Emitter()).listeners) {
                try {
                    if (listener.filter == null || listener.filter.isNotificationEnabled(notification)) {
                        executor.execute(() -> {
                            try {
                                listener.listener.handleNotification(notification, listener.handback);
                            } catch (Exception x) {
                                exceptionConsumer.accept(x);
                            }
                        });
                    }
                } catch (Exception x) {
                    exceptionConsumer.accept(x);
                }
            }
        }

        public static void sendNotification(MaridNotificationEmitter emitter, Notification notification, Consumer<Exception> consumer) {
            sendNotification(emitter, notification, Runnable::run, consumer);
        }

        protected static class ListenerInfo {

            protected final NotificationListener listener;
            protected final NotificationFilter filter;
            protected final Object handback;

            protected ListenerInfo(NotificationListener listener, NotificationFilter filter, Object handback) {
                this.listener = listener;
                this.filter = filter;
                this.handback = handback;
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof ListenerInfo)) {
                    return false;
                } else {
                    final ListenerInfo that = (ListenerInfo) obj;
                    if (that instanceof WildcardListenerInfo) {
                        return listener == that.listener;
                    } else {
                        return listener == that.listener && filter == that.filter && handback == that.handback;
                    }
                }
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(listener);
            }
        }

        protected static class WildcardListenerInfo extends ListenerInfo {

            protected WildcardListenerInfo(NotificationListener listener) {
                super(listener, null, null);
            }
        }
    }
}
