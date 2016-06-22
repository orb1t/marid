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

package org.marid.runtime;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class EventScopedTest {

    @Test
    public void test() {
        try (final AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext(A.class)) {
            final Set<AutoCloseable> closeables = new LinkedHashSet<>();
            try {
                final AnnotationConfigApplicationContext child1 = new AnnotationConfigApplicationContext();
                closeables.add(child1);
                child1.register(B.class);
                child1.setParent(parent);
                child1.refresh();
                child1.getApplicationListeners().forEach(parent::addApplicationListener);
                final AnnotationConfigApplicationContext child2 = new AnnotationConfigApplicationContext();
                closeables.add(child2);
                child2.register(C.class);
                child2.setParent(parent);
                child2.refresh();
                child2.getApplicationListeners().forEach(parent::addApplicationListener);
                parent.getBean(A.class).publish();
                final B b = child1.getBean(B.class);
                final C c = child2.getBean(C.class);
                System.out.println(b.messages);
                System.out.println(c.messages);
            } finally {
                for (final AutoCloseable closeable : closeables) {
                    try {
                        closeable.close();
                    } catch (Exception x) {
                        x.printStackTrace();
                    }
                }
            }
        }
    }

    @Configuration
    static class A {

        private final ApplicationEventPublisher publisher;

        @Autowired
        A(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        public void publish() {
            publisher.publishEvent("test");
        }
    }

    @Configuration
    static class B {

        private final Set<String> messages = new LinkedHashSet<>();

        @EventListener
        public void listen(String message) {
            messages.add(message);
        }
    }

    @Configuration
    static class C {

        private final Set<String> messages = new LinkedHashSet<>();

        @EventListener
        public void listen(String message) {
            messages.add(message);
        }
    }
}
