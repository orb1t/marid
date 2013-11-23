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

import groovy.util.logging.Log
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runners.MethodSorters
import org.marid.nio.FileUtils
import org.marid.test.AbstractMethodProfiler
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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FsTtvStoreTest extends AbstractMethodProfiler {

    private static final def tags = ["tag1", "tag2", "tag3"].toSet();
    private static final def data = new HashMap<String, TreeMap<Date, Double>>();
    private static final def duration = 3;
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
    void test1Insert() {
        def insertResult = ttvStore.insert(Double, data);
        for (def th : insertResult.errors) {
            th.printStackTrace();
        }
    }

    @Test
    void test2Select() {
        def selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.value == data;
    }

    @Test
    void test3SelectAfterInvalidateCache() {
        ttvStore.entryCache.invalidateAll();
        def selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.value == data;
    }

    @Test
    void test4RemoveKeys() {
        def slicePoint = new Date(start.timeInMillis + (int) (duration / 2));
        def removeResult = ttvStore.removeKeys(Double, [tag1: slicePoint]);
        log.info("Remove result: {0}", removeResult.value);
        data["tag1"].remove(slicePoint);
        def selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.value == data;
    }

    @Test
    void test5RemoveRange() {
        def slicePoint = new Date(start.timeInMillis + ((int) (duration / 2)) * 1000L);
        def removeResult = ttvStore.removeAfter(Double, ["tag2"].toSet(), slicePoint, false);
        for (def th : removeResult.errors) {
            th.printStackTrace();
        }
        log.info("Remove result: {0}", removeResult.value);
        for (def k in data["tag2"].tailMap(slicePoint, false).keySet().toList()) {
            data["tag2"].remove(k);
        }
        def selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.value == data;
    }
}
