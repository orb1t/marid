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
package org.marid.db.data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Numeric data record.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class NumericDataRecord<T extends Number> extends DataRecord<T>{

    /**
     * Default constructor.
     */
    public NumericDataRecord() {
        this("", 0L);
    }

    /**
     * Constructs a numeric data record.
     * @param tg Tag.
     * @param ts Timestamp.
     */
    public NumericDataRecord(String tg, long ts) {
        super(tg, ts);
    }

    /**
     * Get a byte value.
     * @return Byte value.
     */
    public byte getByte() {
        return getValue().byteValue();
    }

    /**
     * Get a short value.
     * @return Short value.
     */
    public short getShort() {
        return getValue().shortValue();
    }

    /**
     * Get an integer value.
     * @return Integer value.
     */
    public int getInt() {
        return getValue().intValue();
    }

    /**
     * Get a long value.
     * @return Long value.
     */
    public long getLong() {
        return getValue().longValue();
    }

    /**
     * Get a float value.
     * @return Float value.
     */
    public float getFloat() {
        return getValue().floatValue();
    }

    /**
     * Get a double value.
     * @return Double value.
     */
    public double getDouble() {
        return getValue().doubleValue();
    }

    /**
     * Get a big integer.
     * @return Big integer.
     */
    public BigInteger getBigint() {
        Number val = getValue();
        if (val instanceof BigInteger) {
            return (BigInteger)val;
        } else if (val instanceof BigDecimal) {
            return ((BigDecimal)val).toBigInteger();
        } else {
            return BigInteger.valueOf(val.longValue());
        }
    }

    /**
     * Get a big decimal.
     * @return Big decimal.
     */
    public BigDecimal getBigDecimal() {
        Number val = getValue();
        if (val instanceof BigDecimal) {
            return (BigDecimal)val;
        } else if (val instanceof BigInteger) {
            return new BigDecimal((BigInteger)val);
        } else if (val instanceof Long) {
            return new BigDecimal(val.longValue());
        } else if (val instanceof Integer) {
            return new BigDecimal(val.intValue());
        } else {
            return new BigDecimal(val.doubleValue());
        }
    }
}
