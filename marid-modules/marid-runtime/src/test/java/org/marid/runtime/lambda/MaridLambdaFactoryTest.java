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

package org.marid.runtime.lambda;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicStampedReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaridLambdaFactoryTest {

    @Test
    public void testRunnableWithoutArguments() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        try (final PrintWriter printWriter = new PrintWriter(stringWriter)) {
            final Runnable runnable = MaridLambdaFactory.lambda(
                    Runnable.class,
                    PrintWriter.class.getMethod("println"),
                    new AtomicStampedReference<>(printWriter, 0)
            );
            runnable.run();
        }
        assertEquals(System.lineSeparator(), stringWriter.toString());
    }
}
