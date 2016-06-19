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

package org.marid.spring.util;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class TypedApplicationEventListener<T extends ApplicationEvent> implements ApplicationListener<T> {

    private final Class<T> eventType;
    private final ApplicationListener<T> delegate;

    public TypedApplicationEventListener(Class<T> eventType, ApplicationListener<T> delegate) {
        this.eventType = eventType;
        this.delegate = delegate;
    }

    @Override
    public void onApplicationEvent(T event) {
        if (eventType.isInstance(event)) {
            delegate.onApplicationEvent(event);
        }
    }

    public static <T extends ApplicationEvent> void listen(ConfigurableApplicationContext context,
                                                           Class<T> eventType,
                                                           ApplicationListener<T> listener) {
        context.addApplicationListener(new TypedApplicationEventListener<>(eventType, listener));
    }
}
