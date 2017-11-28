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

import org.marid.beans.BeanTypeContext;
import org.marid.misc.Calls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Type;
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

public interface ConstExpression extends ValueExpression {

  @Nonnull
  ConstantType getType();

  @Nonnull
  @Override
  default Type getType(@Nullable Type owner, @Nonnull BeanTypeContext context) {
    return getType().type;
  }

  enum ConstantType {

    Z(boolean.class, Boolean::valueOf),
    B(byte.class, Byte::decode),
    S(short.class, Short::decode),
    I(int.class, Integer::decode),
    J(long.class, Long::decode),
    C(char.class, v -> (char) (int) Integer.decode(v)),
    F(float.class, Float::valueOf),
    D(double.class, Double::valueOf),
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
    VOID(Void.class, s -> null);

    public final Class<?> type;
    public final Function<String, ?> converter;

    <T> ConstantType(Class<T> type, Function<String, T> converter) {
      this.type = type;
      this.converter = converter;
    }
  }
}
