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

package org.marid.service;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractServiceTest {

    @Test(expected = TimeoutException.class)
    public void testDelayTimeout() throws Exception {
        final AbstractService service = new TestService(1000L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200L);
                    service.start();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
        service.send('a').get(300L, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testNormal() throws Exception {
        final AbstractService service = new TestService(1000L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200L);
                    service.start();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
        Future<?> future = service.send('a');
        Integer response = (Integer) future.get(1220L, TimeUnit.MILLISECONDS);
        assertEquals(new Integer(1), response);
    }

    @Test(expected = CancellationException.class)
    public void testCancellationInterrupted() throws Exception {
        final AbstractService service = new TestService(1000L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200L);
                    service.start();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
        final Future<?> future = service.send('a');
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100L);
                    future.cancel(true);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
        Integer response = (Integer) future.get(1220L, TimeUnit.MILLISECONDS);
        assertEquals(new Integer(1), response);
    }

    @Test(expected = CancellationException.class)
    public void testCancellationInterrupted2() throws Exception {
        final AbstractService service = new TestService(1000L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200L);
                    service.start();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
        final Future<?> future = service.send('a');
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(400L);
                    future.cancel(true);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
        Integer response = (Integer) future.get(1220L, TimeUnit.MILLISECONDS);
        assertEquals(new Integer(1), response);
    }

    @Test(expected = CancellationException.class)
    public void testCancellationInterrupted3() throws Exception {
        final AbstractService service = new TestService(1000L);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200L);
                    service.start();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
        final Future<?> future = service.send('a', 2);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(400L);
                    future.cancel(true);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
        List response = (List) future.get(1220L, TimeUnit.MILLISECONDS);
        assertEquals(Arrays.asList(1, 2), response);
    }

    private class TestService extends AbstractService {

        private final ExecutorService executorService = Executors.newSingleThreadExecutor();
        private final long delay;

        public TestService(long delay) {
            super("testId", "test", new ServiceDescriptor());
            this.delay = delay;
        }

        @Override
        protected void doStart() throws Exception {
        }

        @Override
        protected void doStop() throws Exception {
            executorService.shutdown();
        }

        @Override
        protected Future<?> doSend(final Object message) {
            return executorService.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Thread.sleep(delay);
                    return 1;
                }
            });
        }

        @Override
        protected Future<List<?>> doSend(Object... messages) {
            return executorService.submit(new Callable<List<?>>() {
                @Override
                public List<?> call() throws Exception {
                    Thread.sleep(delay);
                    return Arrays.asList(1, 2);
                }
            });
        }
    }
}
