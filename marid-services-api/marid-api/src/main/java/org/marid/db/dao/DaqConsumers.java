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
