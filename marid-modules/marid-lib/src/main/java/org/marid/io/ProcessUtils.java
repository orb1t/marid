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

package org.marid.io;

import java.lang.reflect.Field;
import java.util.concurrent.TimeoutException;

/**
 * @author Dmitry Ovchinnikov
 */
public class ProcessUtils {

    private static final ClassValue<Field> PID_FIELDS = new ClassValue<Field>() {
        @Override
        protected Field computeValue(Class<?> type) {
            try {
                for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                    final Field field;
                    try {
                        field = c.getDeclaredField("pid");
                        if (field.getType() != int.class) {
                            continue;
                        }
                    } catch (NoSuchFieldException x) {
                        continue;
                    }
                    field.setAccessible(true);
                    return field;
                }
            } catch (Exception x) {
                return null;
            }
            return null;
        }
    };

    public static int joinProcess(Process process, long timeout) throws InterruptedException, TimeoutException {
        final long startTime = System.currentTimeMillis();
        do {
            try {
                return process.exitValue();
            } catch (IllegalThreadStateException x) {
                Thread.sleep(10L);
            }
        } while (System.currentTimeMillis() - startTime <= timeout);
        throw new TimeoutException();
    }

    public static int getPid(Process process) {
        final Field field = PID_FIELDS.get(process.getClass());
        if (field != null) {
            try {
                return (int) field.get(process);
            } catch (ReflectiveOperationException x) {
                throw new IllegalStateException(x);
            }
        }
        return -1;
    }
}
