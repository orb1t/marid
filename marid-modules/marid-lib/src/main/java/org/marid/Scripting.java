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

package org.marid;

import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class Scripting {

    public static final Scripting SCRIPTING;

    static {
        Iterator<Scripting> it = ServiceLoader.load(Scripting.class).iterator();
        SCRIPTING = it.hasNext() ? it.next() : null;
    }

    public abstract boolean isFunction(Object object);

    public abstract boolean isDelegatingSupported();

    public abstract boolean isComposingSupported();

    public abstract boolean isCurrySupported();

    public abstract boolean isPropertySupported();

    public abstract Object call(Object function, Object... args);

    public abstract boolean callPredicate(Object predicate, Object... args);

    public abstract Object compose(Object firstFunction, Object secondFunction);

    public abstract Object compose(Object... functions);

    public abstract Object curry(Object function, Object... args);

    public abstract Object curryTail(Object function, Object... args);

    public abstract void setDelegate(Object function, Object delegate);

    public abstract void setProperty(Object function, String property, Object value);

    public abstract Object getProperty(Object function, String property);

    public abstract <T> T cast(Class<T> type, Object object);

    public abstract int hashCode(Object object);

    public abstract String toString(Object object);

    public abstract boolean equals(Object o1, Object o2);

    public abstract Object eval(URL url, Map<String, Object> bindings);

    public abstract Object eval(URL url);

    public abstract Object eval(File file, Map<String, Object> bindings);

    public abstract Object eval(File file);

    public abstract Object eval(Path path, Map<String, Object> bindings);

    public abstract Object eval(Path path);

    public abstract Object eval(String code, String name, Map<String, Object> bindings);

    public abstract Object eval(String code, String name);

    public abstract Object eval(Reader reader, String name, Map<String, Object> bindings);

    public abstract Object eval(Reader reader, String name);

    public abstract String replace(String source, Map<String, Object> bindings);

    public abstract ClassLoader getClassLoader();

    public abstract String getMime();

    public abstract String getExtension();
}
