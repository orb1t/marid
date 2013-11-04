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

package org.marid.dpp

import groovy.util.logging.Log
import org.junit.Test
import org.junit.experimental.categories.Category
import org.marid.test.SlowTests

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author Dmitry Ovchinnikov
 */
@Category([SlowTests])
@Log
class DppTest {

    @Test
    void testCount() {
        def locker = new CountDownLatch(3);
        def scheduler = new DppScheduler("scheduler", [
            buses : [
                bus0 : [
                    groups : [
                        group0 : [
                            period : 1L,
                            tasks : [
                                task0 : [
                                    vars : [
                                        count : 0
                                    ],
                                    func : {t, r ->
                                        log.info("#{0}", t["count"]);
                                        locker.countDown();
                                        if (t["count"]++ > 3) {
                                            t.stop();
                                        }
                                    }
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]);
        scheduler.start();
        locker.await(4L, TimeUnit.SECONDS);
    }
}
