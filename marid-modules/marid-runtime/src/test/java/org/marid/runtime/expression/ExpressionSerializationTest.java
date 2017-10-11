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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.marid.io.Xmls;
import org.marid.misc.Casts;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.marid.runtime.expression.ConstantExpr.*;
import static org.marid.runtime.expression.ConstantExpression.ConstantType.*;
import static org.marid.runtime.expression.NullExpr.NULL;
import static org.marid.runtime.expression.ThisExpr.THIS;

class ExpressionSerializationTest {

    @Test
    void testNull() {
        check(new NullExpr(), (e, a) -> {});
    }

    private static Stream<? extends ValueExpression> valueClasses() {
        return Stream.of(
                boolExpr("x"),
                intExpr("x"),
                longExpr("x"),
                shortExpr("x"),
                byteExpr("x"),
                charExpr("x"),
                doubleExpr("x"),
                floatExpr("x")
        );
    }

    @ParameterizedTest
    @MethodSource("valueClasses")
    void testValues(ValueExpr expr) {
        check(expr, (e, a) -> assertEquals(e.getValue(), a.getValue()));
    }

    @Test
    void testString() {
        check(new StringExpr("x"), (e, a) -> assertEquals(e.getValue(), a.getValue()));
    }

    @Test
    void testRef() {
        check(new RefExpr("z"), (e, a) -> assertEquals(e.getReference(), a.getReference()));
    }

    @Test
    void testMethodCall() {
        check(new MethodCallExpr(
                        boolExpr("x"),
                        "x",
                        new MethodCallStaticExpr(
                                new ConstructorCallExpr(NULL, THIS),
                                "v",
                                intExpr("0"),
                                longExpr("1")
                        ),
                        new RefExpr("u")),
                (e, a) -> {
                    final ConstantExpr expectedTarget = (ConstantExpr) e.getTarget();
                    final ConstantExpr actualTarget = (ConstantExpr) a.getTarget();
                    assertEquals(expectedTarget.getValue(), actualTarget.getValue());
                    assertEquals(BOOL, actualTarget.getType());

                    assertEquals(e.getMethod(), a.getMethod());

                    final MethodCallStaticExpr expectedArg0 = (MethodCallStaticExpr) e.getArgs().get(0);
                    final MethodCallStaticExpr actualArg0 = (MethodCallStaticExpr) a.getArgs().get(0);

                    assertEquals(expectedArg0.getMethod(), actualArg0.getMethod());

                    final ConstructorCallExpr actualArg0Target = (ConstructorCallExpr) actualArg0.getTarget();

                    assertSame(NULL, actualArg0Target.getTarget());
                    assertSame(THIS, actualArg0Target.getArgs().get(0));

                    final ConstantExpr arg0 = (ConstantExpr) actualArg0.getArgs().get(0);
                    final ConstantExpr arg1 = (ConstantExpr) actualArg0.getArgs().get(1);

                    assertEquals("0", arg0.getValue());
                    assertEquals(INT, arg0.getType());

                    assertEquals("1", arg1.getValue());
                    assertEquals(LONG, arg1.getType());
                });
    }

    private <T extends Expression> void check(T expected, BiConsumer<T, T> checker) {
        final StringWriter writer = new StringWriter();
        try (writer) {
            Xmls.writeFormatted(expected::to, new StreamResult(writer));
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }

        final StringReader reader = new StringReader(writer.toString());
        try (reader) {
            final Expression expression = Xmls.read(reader, NULL::from);

            final Class<T> type = Casts.cast(expected.getClass());
            assertTrue(type.isInstance(expression));
            checker.accept(expected, type.cast(expression));
        }
    }
}
