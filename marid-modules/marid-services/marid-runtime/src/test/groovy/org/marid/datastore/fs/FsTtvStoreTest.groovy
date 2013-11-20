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

package org.marid.datastore.fs

import com.carrotsearch.junitbenchmarks.AbstractBenchmark
import com.carrotsearch.junitbenchmarks.BenchmarkOptions
import groovy.util.logging.Log
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runners.MethodSorters
import org.marid.nio.FileUtils
import org.marid.test.SlowTests

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ThreadLocalRandom

/**
 * @author Dmitry Ovchinnikov
 */
@SuppressWarnings("GroovyAccessibility")
@Category([SlowTests])
@Log
@BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FsTtvStoreTest extends AbstractBenchmark {

    private static final def tags = ["tag1", "tag2", "tag3"].toSet();
    private static final def data = new HashMap<String, TreeMap<Date, Double>>();
    private static final def duration = 48 * 3600;
    private static final def start = new GregorianCalendar(2000, 0, 1, 0, 0, 0);
    private static final def end = (GregorianCalendar) start.clone();
    private static Path storePath;
    private static FsTtvStore ttvStore;

    @BeforeClass
    static void init() {
        end.add(Calendar.SECOND, duration);
        storePath = Files.createTempDirectory("test");
        ttvStore = new FsTtvStore([dir: storePath]);
        ttvStore.startAsync().awaitRunning();
        log.info("Test started on {0}", storePath);
        for (def tag in tags) {
            def random = ThreadLocalRandom.current();
            def values = new TreeMap<Date, Double>();
            for (def seconds in 0..duration) {
                values[new Date(start.timeInMillis + seconds * 1000L)] = random.nextDouble();
            }
            data[tag] = values;
        }
    }

    @AfterClass
    static void destroy() {
        ttvStore.clear();
        ttvStore.stopAsync().awaitTerminated();
        Files.walkFileTree(storePath, FileUtils.RECURSIVE_CLEANER);
        if (Files.exists(storePath)) {
            log.warning("{0} exists", storePath);
        }
        log.info("Test finished on {0}", storePath)
    }

    @Test
    void testInsert() {
        def insertResult = ttvStore.insert(Double, data);
        log.info("Insert result: {0}; time = {1} s", insertResult);
        for (def th : insertResult.errors) {
            th.printStackTrace();
        }
    }

    @Test
    void testSelect() {
        def selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        log.info("Selection result: {0}; time = {1} s", selectResult, sw());
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.value == data;
    }

    @Test
    void testSelectAfterInvalidateCache() {
        ttvStore.entryCache.invalidateAll();
        def selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        log.info("Selection result after cleaning cache: {0}; time = {1} s", selectResult, sw());
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.value == data;
    }

    @Test
    void testRemoveKeys() {
        def slicePoint = new Date(start.timeInMillis + (int) (duration / 2));
        ttvStore.removeKeys(Double, [tag1: slicePoint]);
        data["tag1"].remove(slicePoint);
        def selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.value == data;
    }
}
