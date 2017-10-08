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

package org.marid.runtime.expression;

import org.junit.jupiter.api.Test;
import org.marid.io.Xmls;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionSerializationTest {

    @Test
    void testNull() throws Exception {
        final NullExpr expr = new NullExpr();

        final StringWriter writer = new StringWriter();

        try (writer) {
            Xmls.writeFormatted(expr::to, new StreamResult(writer));
        }

        final StringReader reader = new StringReader(writer.toString());

        try (reader) {
            final Expression expression = Xmls.read(reader, NullExpr.NULL::from);

            assertTrue(expression instanceof NullExpression);
        }
    }

    private <T extends Expression> void check(Class<T> type, Consumer<T> configurator, BiConsumer<T, T> checker){
        final T expected;
        try {
            expected = type.getConstructor().newInstance();
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
        configurator.accept(expected);

        final StringWriter writer = new StringWriter();

        try (writer) {
            Xmls.writeFormatted(expected::to, new StreamResult(writer));
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }

        final StringReader reader = new StringReader(writer.toString());

        try (reader) {
            final Expression expression = Xmls.read(reader, NullExpr.NULL::from);

            assertTrue(expression instanceof NullExpression);
        }
    }
}
