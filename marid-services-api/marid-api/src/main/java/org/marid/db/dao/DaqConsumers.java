/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.db.dao;

import org.marid.db.data.DataRecord;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public interface DaqConsumers {

    static Consumer<Float> floatWriter(NumericWriter numericWriter, long tag) {
        return val -> {
            final DataRecord<Double> record = new DataRecord<>(tag, System.currentTimeMillis(), val.doubleValue());
            numericWriter.merge(Collections.singletonList(record), true);
        };
    }

    static Consumer<Double> doubleWriter(NumericWriter numericWriter, long tag) {
        return val -> {
            final DataRecord<Double> record = new DataRecord<>(tag, System.currentTimeMillis(), val);
            numericWriter.merge(Collections.singletonList(record), true);
        };
    }

    static Consumer<Integer> intWriter(NumericWriter numericWriter, long tag) {
        return val -> {
            final DataRecord<Double> record = new DataRecord<>(tag, System.currentTimeMillis(), val.doubleValue());
            numericWriter.merge(Collections.singletonList(record), true);
        };
    }

    static Consumer<Long> longWriter(NumericWriter numericWriter, long tag) {
        return val -> {
            final DataRecord<Double> record = new DataRecord<>(tag, System.currentTimeMillis(), val.doubleValue());
            numericWriter.merge(Collections.singletonList(record), true);
        };
    }
}
