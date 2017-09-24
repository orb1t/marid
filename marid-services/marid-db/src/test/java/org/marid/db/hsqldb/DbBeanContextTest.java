/*-
 * #%L
 * marid-runtime
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

package org.marid.db.hsqldb;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.marid.runtime.context2.BeanConfiguration;
import org.marid.runtime.context2.BeanContext;
import org.marid.runtime.model.MaridRuntimeBean;

import java.io.File;
import java.util.Properties;

import static org.marid.runtime.expression.ExpressionHelper.*;

@Tag("manual")
class DbBeanContextTest {

    private static final ClassLoader CLASS_LOADER = Thread.currentThread().getContextClassLoader();
    private static final Properties PROPERTIES = new Properties();

    static {
        PROPERTIES.setProperty("file", new File(System.getProperty("user.home"), "test").getAbsolutePath());
    }

    @Test
    void testDb() {
        final BeanConfiguration configuration = new BeanConfiguration(CLASS_LOADER, PROPERTIES);
        final MaridRuntimeBean root = new MaridRuntimeBean()
                .add("db", $(HsqldbDatabase.class, $init($(HsqldbProperties.class),
                        $(THIS, "setDirectory", $file("${file}"))
                )))
                .getParent();
        try (final BeanContext context = new BeanContext(configuration, root)) {

        }
    }
}
