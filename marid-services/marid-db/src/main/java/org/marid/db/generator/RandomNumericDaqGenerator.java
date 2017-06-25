package org.marid.db.generator;

import org.marid.concurrent.MaridTimerTask;
import org.marid.db.dao.NumericWriter;
import org.marid.db.data.DataRecord;
import org.marid.db.data.DataRecordKey;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 */
public class RandomNumericDaqGenerator {

    private final NumericWriter numericWriter;
    private final long tag;
    private final double min;
    private final double max;
    private final long periodMillis;
    private final Timer timer = new Timer();

    public RandomNumericDaqGenerator(NumericWriter numericWriter, long tag, double min, double max, long periodSeconds) {
        this.numericWriter = numericWriter;
        this.tag = tag;
        this.min = min;
        this.max = max;
        this.periodMillis = TimeUnit.SECONDS.toMillis(periodSeconds);
    }

    @PostConstruct
    public void start() {
        timer.schedule(new MaridTimerTask(task -> {
            final long t = System.currentTimeMillis();
            final double v = ThreadLocalRandom.current().nextDouble(min, max);
            final Set<DataRecordKey> result = numericWriter.merge(singletonList(new DataRecord<>(tag, t, v)), true);
            if (!result.isEmpty()) {
                log(INFO, "Generated {0} {1} {2}", tag, t, v);
            }
        }), periodMillis, periodMillis);
    }

    @PreDestroy
    public void stop() {
        timer.cancel();
    }
}
