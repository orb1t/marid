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
import org.marid.service.data.DynRequest;
import org.marid.service.data.DynResponse;
import org.marid.service.data.Request;
import org.marid.service.data.Response;

import java.util.Map;
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
        service.send(new DynRequest<Integer>('a')).get(300L, TimeUnit.MILLISECONDS);
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
        Future<DynResponse> future = service.send(new DynRequest<Integer>('a'));
        DynResponse response = future.get(1220L, TimeUnit.MILLISECONDS);
        assertEquals(1, response.code);
        assertEquals("test", response.error);
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
        final Future<DynResponse> future = service.send(new DynRequest<Integer>('a'));
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
        DynResponse response = future.get(1220L, TimeUnit.MILLISECONDS);
        assertEquals(1, response.code);
        assertEquals("test", response.error);
    }

    @Test(expected = CancellationException.class)
    public void testCancellationInterruptedSecond() throws Exception {
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
        final Future<DynResponse> future = service.send(new DynRequest<Integer>('a'));
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
        DynResponse response = future.get(1220L, TimeUnit.MILLISECONDS);
        assertEquals(1, response.code);
        assertEquals("test", response.error);
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
        protected <T extends Response> Future<T> doSend(final Request<T> message) {
            return executorService.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    Thread.sleep(delay);
                    return message.getErrorResponse(1, "test");
                }
            });
        }

        @Override
        protected Transaction doTransaction(Map<String, Object> params) {
            return new Transaction() {
                @Override
                public Service getService() {
                    return null;
                }

                @Override
                public <T extends Response> Future<T> submit(Request<T> request) {
                    return null;
                }

                @Override
                public Future<TransactionResult> send() {
                    return null;
                }
            };
        }
    }
}
