/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.groovy;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DSL loader.
 */
public class DslLoader {

    private static final Logger log = Logger.getLogger(DslLoader.class.getName());

    /**
     * Loads DSLs.
     * @param classLoader Class loader.
     * @throws Exception An exception during DSL loading.
     */
    public static void loadDsl(ClassLoader classLoader) throws Exception {
        ServiceLoader<Dsl> dslLoader = ServiceLoader.load(Dsl.class, classLoader);
        try {
            for (Dsl dsl : dslLoader) {
                dsl.load();
                log.log(Level.CONFIG, "DSL {0} loaded", dsl);
            }
        } finally {
            dslLoader.reload();
        }
    }

    /**
     * Loads DSLs.
     * @throws Exception An exception during DSLs loading.
     */
    public static void loadDsl() throws Exception {
        loadDsl(Thread.currentThread().getContextClassLoader());
    }
}
