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

package org.marid.expression.generic;

import org.marid.misc.Calls;
import org.marid.runtime.context.BeanContext;
import org.marid.runtime.util.ReflectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Pattern;

public interface ConstExpression extends ValueExpression {

    Pattern ARRAY_ELEMENT_SEPARATOR = Pattern.compile("(\\s*,\\s*)|\\s+", Pattern.MULTILINE);

    byte[] EMPTY_BYTES = new byte[0];
    short[] EMPTY_SHORTS = new short[0];
    char[] EMPTY_CHARS = new char[0];
    int[] EMPTY_INTS = new int[0];
    long[] EMPTY_LONGS = new long[0];
    boolean[] EMPTY_BOOLS = new boolean[0];
    float[] EMPTY_FLOATS = new float[0];
    double[] EMPTY_DOUBLES = new double[0];

    @Nonnull
    ConstantType getType();

    @Nullable
    @Override
    default Object evaluate(@Nullable Object self, @Nonnull BeanContext context) {
        final String v = context.resolvePlaceholders(getValue()).trim();
        if (v.isEmpty()) {
            return null;
        } else {
            final Object result = getType().converter.apply(v);
            return ReflectUtils.eval(result, this, context);
        }
    }

    enum ConstantType {

        BOOL(Boolean.class, Boolean::valueOf),
        BYTE(Byte.class, Byte::decode),
        SHORT(Short.class, Short::decode),
        INT(Integer.class, Integer::decode),
        LONG(Long.class, Long::decode),
        CHAR(Character.class, v -> (char) (int) Integer.decode(v)),
        FLOAT(Float.class, Float::valueOf),
        DOUBLE(Double.class, Double::valueOf),
        BIGDECIMAL(BigDecimal.class, BigDecimal::new),
        BIGINT(BigInteger.class, BigInteger::new),
        FILE(File.class, File::new),
        PATH(Path.class, Paths::get),
        DATE(Date.class, Date::valueOf),
        TIMESTAMP(Timestamp.class, Timestamp::valueOf),
        INSTANT(Instant.class, Instant::parse),
        DURATION(Duration.class, Duration::parse),
        TIME_ZONE(TimeZone.class, TimeZone::getTimeZone),
        ZONE_ID(ZoneId.class, ZoneId::of),
        CURRENCY(Currency.class, Currency::getInstance),
        LOCALE(Locale.class, Locale::forLanguageTag),
        ZONED_DATE_TIME(ZonedDateTime.class, ZonedDateTime::parse),
        LOCAL_DATE_TIME(LocalDateTime.class, LocalDateTime::parse),
        OFFSET_TIME(OffsetTime.class, OffsetTime::parse),
        PERIOD(Period.class, Period::parse),
        YEAR_MONTH(YearMonth.class, YearMonth::parse),
        ZONE_OFFSET(ZoneOffset.class, ZoneOffset::of),
        YEAR(Year.class, Year::parse),
        MONTH(Month.class, Month::valueOf),
        MONTH_DAY(MonthDay.class, MonthDay::parse),
        DAY_OF_WEEK(DayOfWeek.class, DayOfWeek::valueOf),
        INET_ADDR(InetAddress.class, v -> Calls.call(() -> InetAddress.getByName(v))),
        UUID(java.util.UUID.class, java.util.UUID::fromString),
        UBYTES(byte[].class, ConstExpression::ubytes),
        UINTS(int[].class, ConstExpression::uints),
        ULONGS(long[].class, ConstExpression::ulongs),
        FLOATS(float[].class, ConstExpression::floats),
        DOUBLES(double[].class, ConstExpression::doubles);

        public final Class<?> type;
        public final Function<String, ?> converter;

        <T> ConstantType(Class<T> type, Function<String, T> converter) {
            this.type = type;
            this.converter = converter;
        }
    }

    @Nonnull
    private static byte[] ubytes(@Nonnull String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            final String v = value.substring(1, value.length() - 1).trim();
            if (v.isEmpty()) {
                return EMPTY_BYTES;
            } else {
                final String[] parts = ARRAY_ELEMENT_SEPARATOR.split(v);
                final byte[] bytes = new byte[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    bytes[i] = Integer.decode(parts[i]).byteValue();
                }
                return bytes;
            }
        } else {
            throw new IllegalArgumentException(value);
        }
    }

    @Nonnull
    private static int[] uints(@Nonnull String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            final String v = value.substring(1, value.length() - 1).trim();
            if (v.isEmpty()) {
                return EMPTY_INTS;
            } else {
                final String[] parts = ARRAY_ELEMENT_SEPARATOR.split(v);
                final int[] ints = new int[parts.length];
                for (int i = 0; i < ints.length; i++) {
                    if (parts[i].startsWith("0x")) {
                        ints[i] = Integer.parseUnsignedInt(parts[i].substring(2), 16);
                    } else {
                        ints[i] = Integer.parseUnsignedInt(parts[i]);
                    }
                }
                return ints;
            }
        } else {
            throw new IllegalArgumentException(value);
        }
    }

    @Nonnull
    private static long[] ulongs(@Nonnull String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            final String v = value.substring(1, value.length() - 1).trim();
            if (v.isEmpty()) {
                return EMPTY_LONGS;
            } else {
                final String[] parts = ARRAY_ELEMENT_SEPARATOR.split(v);
                final long[] longs = new long[parts.length];
                for (int i = 0; i < longs.length; i++) {
                    if (parts[i].startsWith("0x")) {
                        longs[i] = Long.parseUnsignedLong(parts[i].substring(2), 16);
                    } else {
                        longs[i] = Long.parseUnsignedLong(parts[i]);
                    }
                }
                return longs;
            }
        } else {
            throw new IllegalArgumentException(value);
        }
    }

    @Nonnull
    private static float[] floats(@Nonnull String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            final String v = value.substring(1, value.length() - 1).trim();
            if (v.isEmpty()) {
                return EMPTY_FLOATS;
            } else {
                final String[] parts = ARRAY_ELEMENT_SEPARATOR.split(v);
                final float[] floats = new float[parts.length];
                for (int i = 0; i < floats.length; i++) {
                    floats[i] = Float.parseFloat(parts[i]);
                }
                return floats;
            }
        } else {
            throw new IllegalArgumentException(value);
        }
    }

    @Nonnull
    private static double[] doubles(@Nonnull String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            final String v = value.substring(1, value.length() - 1).trim();
            if (v.isEmpty()) {
                return EMPTY_DOUBLES;
            } else {
                final String[] parts = ARRAY_ELEMENT_SEPARATOR.split(v);
                final double[] doubles = new double[parts.length];
                for (int i = 0; i < doubles.length; i++) {
                    doubles[i] = Double.parseDouble(parts[i]);
                }
                return doubles;
            }
        } else {
            throw new IllegalArgumentException(value);
        }
    }
}
