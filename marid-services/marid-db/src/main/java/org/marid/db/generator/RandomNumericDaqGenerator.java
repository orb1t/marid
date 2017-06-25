package org.marid.db.generator;

/*-
 * #%L
 * marid-db
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
