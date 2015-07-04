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

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.marid.Marid;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridTestRunner extends BlockJUnit4ClassRunner {

    public MaridTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    for (Class<?> c = getTestClass().getJavaClass(); c != null; c = c.getSuperclass()) {
                        final ContextConfiguration contextConfiguration = c.getAnnotation(ContextConfiguration.class);
                        if (contextConfiguration == null) {
                            continue;
                        }
                        if (!contextConfiguration.inheritLocations() && c != getTestClass().getJavaClass()) {
                            continue;
                        }
                        if (contextConfiguration.classes().length > 0) {
                            Marid.CONTEXT.register(contextConfiguration.classes());
                        }
                        if (contextConfiguration.locations().length > 0) {
                            Marid.CONTEXT.scan(contextConfiguration.locations());
                        }
                    }
                    Marid.start(Runnable::run);
                } finally {
                    statement.evaluate();
                }
            }
        };
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    Marid.CONTEXT.close();
                } finally {
                    statement.evaluate();
                }
            }
        };
    }

    @Override
    protected Object createTest() throws Exception {
        final Object object = super.createTest();
        Marid.CONTEXT.getAutowireCapableBeanFactory().autowireBean(object);
        return object;
    }
}
