/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * @author Dmitry Ovchinnikov
 */
class MaridSignalHandler {

    static void install(Runnable contextCloseTask) {
        try {
            final Class<?> signalClass = Class.forName("sun.misc.Signal");
            final Class<?> signalHandlerItf = Class.forName("sun.misc.SignalHandler");
            final Method handleMethod = signalClass.getMethod("handle", signalClass, signalHandlerItf);
            final Constructor<?> signalConstructor = signalClass.getConstructor(String.class);
            final Object intSignal = signalConstructor.newInstance("INT");
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            final Object signalHandler = newProxyInstance(cl, new Class<?>[] {signalHandlerItf}, (o, m, args) -> {
                switch (m.getName()) {
                    case "handle":
                        contextCloseTask.run();
                        break;
                }
                return null;
            });
            handleMethod.invoke(null, intSignal, signalHandler);
        } catch (Throwable x) {
            System.err.println("Unable to install SIGINT handler");
        }
    }
}
