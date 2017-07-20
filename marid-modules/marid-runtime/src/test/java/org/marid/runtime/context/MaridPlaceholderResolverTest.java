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

package org.marid.runtime.context;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.runtime.context.MaridPlaceholderResolver.CircularPlaceholderException;
import org.marid.test.NormalTests;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class MaridPlaceholderResolverTest {

    @Test(expected = CircularPlaceholderException.class)
    public void circular1() {
        final Properties properties = new Properties();
        properties.setProperty("x1", "2");
        properties.setProperty("x2", "${x3}");
        properties.setProperty("x3", "${x2}");
        final MaridPlaceholderResolver resolver = new MaridPlaceholderResolver(properties);
        resolver.resolvePlaceholders("abc ${x2}");
    }

    @Test(expected = CircularPlaceholderException.class)
    public void circular2() {
        final Properties properties = new Properties();
        properties.setProperty("x1", "2");
        properties.setProperty("x2", "${x2}");
        final MaridPlaceholderResolver resolver = new MaridPlaceholderResolver(properties);
        resolver.resolvePlaceholders("abc ${x2}");
    }

    @Test
    public void defValue() {
        final Properties properties = new Properties();
        properties.setProperty("x1", "2");
        final MaridPlaceholderResolver resolver = new MaridPlaceholderResolver(properties);
        assertEquals("abc zz 2", resolver.resolvePlaceholders("abc ${x2:zz} ${x1}"));
    }

    @Test
    public void unterminated() {
        final Properties properties = new Properties();
        properties.setProperty("x1", "2");
        final MaridPlaceholderResolver resolver = new MaridPlaceholderResolver(properties);
        assertEquals("abc zz ${x1", resolver.resolvePlaceholders("abc ${x2:zz} ${x1"));
        assertEquals("abc  ${x1", resolver.resolvePlaceholders("abc ${x2} ${x1"));
    }
}
