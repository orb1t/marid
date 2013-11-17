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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.marid.nio.FileUtils
import org.marid.test.SlowTests

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

/**
 * @author Dmitry Ovchinnikov
 */
@SuppressWarnings("GroovyAccessibility")
@Category([SlowTests])
@Log
class FsTtvStoreTest {

    private Path storePath;
    private FsTtvStore ttvStore;

    @Before
    void init() {
        storePath = Files.createTempDirectory("test");
        ttvStore = new FsTtvStore([dir: storePath]);
        ttvStore.startAsync().awaitRunning();
        log.info("Test started on {0}", storePath);
    }

    @After
    void destroy() {
        ttvStore.stopAsync().awaitTerminated();
        Files.walkFileTree(storePath, FileUtils.RECURSIVE_CLEANER);
        if (Files.exists(storePath)) {
            log.warning("{0} exists", storePath);
        }
        log.info("Test finished on {0}", storePath)
    }

    @Test
    void testInsert() {
        def data = new ConcurrentHashMap<String, Map<Date, Double>>();
        def duration = 48 * 3600;
        def start = new GregorianCalendar(2000, 0, 1, 0, 0, 0);
        def end = (GregorianCalendar) start.clone();
        end.add(Calendar.SECOND, duration);
        def tags = ["tag1", "tag2", "tag3"].toSet();
        def threads = new LinkedList<Thread>();
        tags.each {tag ->
            threads << Thread.start {
                def random = ThreadLocalRandom.current();
                def values = new TreeMap<Date, Double>();
                for (def seconds in 0..duration) {
                    values[new Date(start.timeInMillis + seconds * 1000L)] = random.nextDouble();
                }
                data[tag] = values;
            }
        }
        for (def th : threads) th.join();
        def insertResult = ttvStore.insert(Double, data);
        log.info("Insert result: {0}", insertResult);
        for (def th : insertResult.errors) {
            th.printStackTrace();
        }
        def selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        log.info("Selection result: {0}", selectResult);
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.result == data;
        ttvStore.entryCache.invalidateAll();
        selectResult = ttvStore.between(Double, tags, start.time, true, end.time, true);
        log.info("Selection result after cleaning cache: {0}", selectResult);
        for (def th : selectResult.errors) {
            th.printStackTrace();
        }
        assert selectResult.result == data;
        ttvStore.clear();
    }

    @Test
    void testRemove() {
        ttvStore.clear();
    }
}
