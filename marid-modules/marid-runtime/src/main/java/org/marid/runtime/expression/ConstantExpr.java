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

import javax.annotation.Nonnull;

import static org.marid.runtime.expression.ConstantExpression.ConstantType.*;

public class ConstantExpr extends ValueExpr implements ConstantExpression {

    private ConstantType type;

    public ConstantExpr(@Nonnull ConstantType type, @Nonnull String value) {
        super(value);
        this.type = type;
    }

    public ConstantExpr() {
        this(INT, "");
    }

    @Nonnull
    @Override
    public ConstantType getType() {
        return type;
    }

    @Override
    public void setType(@Nonnull ConstantType type) {
        this.type = type;
    }

    public static ConstantExpr byteExpr(String value) {
        return new ConstantExpr(BYTE, value);
    }

    public static ConstantExpr intExpr(String value) {
        return new ConstantExpr(INT, value);
    }

    public static ConstantExpr longExpr(String value) {
        return new ConstantExpr(LONG, value);
    }

    public static ConstantExpr floatExpr(String value) {
        return new ConstantExpr(FLOAT, value);
    }

    public static ConstantExpr doubleExpr(String value) {
        return new ConstantExpr(DOUBLE, value);
    }

    public static ConstantExpr charExpr(String value) {
        return new ConstantExpr(CHAR, value);
    }

    public static ConstantExpr shortExpr(String value) {
        return new ConstantExpr(SHORT, value);
    }

    public static ConstantExpr boolExpr(String value) {
        return new ConstantExpr(BOOL, value);
    }
}
