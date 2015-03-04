/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.test;

import org.marid.Marid;
import org.marid.functions.SafeRunnable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;

import java.util.concurrent.CountDownLatch;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridContextLoader implements ContextLoader {

    @Override
    public String[] processLocations(Class<?> clazz, String... locations) {
        final ContextConfiguration conf = clazz.getAnnotation(ContextConfiguration.class);
        if (conf != null) {
            if (conf.classes().length > 0) {
                Marid.getCurrentContext().register(conf.classes());
            }
        }
        return new String[0];
    }

    @Override
    public AnnotationConfigApplicationContext loadContext(String... locations) throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Marid.getCurrentContext().addApplicationListener(event -> {
            if (event instanceof ContextStartedEvent) {
                countDownLatch.countDown();
            }
        });
        new Thread(SafeRunnable.runnable(() -> Marid.start(Runnable::run))).start();
        countDownLatch.await();
        return Marid.getCurrentContext();
    }
}
