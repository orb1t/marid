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

package org.marid.groovy;

import groovy.lang.*;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.*;
import org.codehaus.groovy.runtime.callsite.BooleanClosureWrapper;
import org.marid.Scripting;
import org.marid.util.CollectionUtils;
import org.marid.collections.DelegatedMap;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyScripting extends Scripting {

    private final CompilerConfiguration compilerConfiguration;
    private final GroovyShell shell;
    private final TemplateEngine templateEngine;

    public GroovyScripting() throws Exception {
        compilerConfiguration = compilerConfiguration();
        shell = new GroovyShell(classLoader(), new Binding(), compilerConfiguration);
        templateEngine = new SimpleTemplateEngine(shell);
    }

    private CompilerConfiguration compilerConfiguration() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/groovy.properties")) {
            if (is != null) {
                Properties properties = new Properties();
                properties.load(is);
                return new CompilerConfiguration(properties);
            }
        }
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setSourceEncoding("UTF-8");
        cc.setTargetBytecode("1.7");
        cc.setRecompileGroovySource(true);
        cc.setDebug(true);
        cc.setVerbose(false);
        return cc;
    }

    private ClassLoader classLoader() {
        ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        return currentLoader == null ? getClass().getClassLoader() : currentLoader;
    }

    @Override
    public boolean isFunction(Object object) {
        return object instanceof Closure;
    }

    @Override
    public boolean isDelegatingSupported() {
        return true;
    }

    @Override
    public boolean isComposingSupported() {
        return true;
    }

    @Override
    public boolean isCurrySupported() {
        return true;
    }

    @Override
    public boolean isPropertySupported() {
        return true;
    }

    @Override
    public Object call(Object function, Object... args) {
        return ((Closure) function).call(args);
    }

    @Override
    public boolean callPredicate(Object predicate, Object... args) {
        return new BooleanClosureWrapper((Closure) predicate).call(args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object compose(Object firstFunction, Object secondFunction) {
        return new ComposedClosure((Closure) firstFunction, (Closure) secondFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object compose(Object... functions) {
        if (functions.length == 0) {
            return Closure.IDENTITY;
        } else {
            Object f = functions[0];
            for (int i = 1; i < functions.length; i++) {
                f = new ComposedClosure((Closure) f, (Closure) functions[i]);
            }
            return f;
        }
    }

    @Override
    public Object curry(Object function, Object... args) {
        return ((Closure) function).curry(args);
    }

    @Override
    public Object curryTail(Object function, Object... args) {
        return ((Closure) function).rcurry(args);
    }

    @Override
    public void setDelegate(Object function, Object delegate) {
        ((Closure) function).setDelegate(delegate);
    }

    @Override
    public void setProperty(Object function, String property, Object value) {
        ((GroovyObject) function).setProperty(property, value);
    }

    @Override
    public Object getProperty(Object function, String property) {
        return ((GroovyObject) function).getProperty(property);
    }

    @Override
    public <T> T cast(Class<T> type, Object object) {
        if (object instanceof Collection) {
            return DefaultGroovyMethods.asType((Collection) object, type);
        } else if (object instanceof Map) {
            return DefaultGroovyMethods.asType((Map) object, type);
        } else if (object instanceof Number) {
            return DefaultGroovyMethods.asType((Number) object, type);
        } else if (object instanceof Object[]) {
            return DefaultGroovyMethods.asType((Object[]) object, type);
        } else if (object instanceof Closure) {
            return DefaultGroovyMethods.asType((Closure) object, type);
        } else if (object instanceof GString) {
            return StringGroovyMethods.asType((GString) object, type);
        } else if (object instanceof String) {
            return StringGroovyMethods.asType((String) object, type);
        } else if (object instanceof CharSequence) {
            return StringGroovyMethods.asType((CharSequence) object, type);
        } else if (object instanceof File) {
            return ResourceGroovyMethods.asType((File) object, type);
        } else {
            return DefaultGroovyMethods.asType(object, type);
        }
    }

    @Override
    public int hashCode(Object object) {
        MetaClass metaClass = DefaultGroovyMethods.getMetaClass(object);
        return (int) metaClass.invokeMethod(object, "hashCode", CollectionUtils.EMPTY_ARRAY);
    }

    @Override
    public String toString(Object object) {
        return InvokerHelper.toString(object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o1, Object o2) {
        if (o1 instanceof Map && o2 instanceof Map) {
            return DefaultGroovyMethods.equals((Map) o1, (Map) o2);
        } else if (o1 instanceof List && o2 instanceof List) {
            return DefaultGroovyMethods.equals((List) o1, (List) o2);
        } else if (o1 instanceof List && o2 instanceof Object[]) {
            return DefaultGroovyMethods.equals((List) o1, (Object[]) o2);
        } else if (o1 instanceof Object[] && o2 instanceof List) {
            return DefaultGroovyMethods.equals((Object[]) o1, (List) o2);
        } else if (o1 instanceof int[] && o2 instanceof int[]) {
            return DefaultGroovyMethods.equals((int[]) o1, (int[]) o2);
        } else if (o1 instanceof Set && o2 instanceof Set) {
            return DefaultGroovyMethods.equals((Set) o1, (Set) o2);
        } else {
            return DefaultGroovyMethods.equals(Collections.singleton(o1), Collections.singleton(o2));
        }
    }

    @Override
    public Object eval(URL url, Map<String, Object> bindings) {
        try {
            Script script = shell.parse(new GroovyCodeSource(url));
            script.setBinding(new Binding(bindings));
            return script.run();
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public Object eval(URL url) {
        try {
            return shell.evaluate(new GroovyCodeSource(url));
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public Object eval(File file, Map<String, Object> bindings) {
        try {
            Script script = shell.parse(file);
            script.setBinding(new Binding(bindings));
            return script.run();
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public Object eval(File file) {
        try {
            return shell.evaluate(file);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public Object eval(Path path, Map<String, Object> bindings) {
        return eval(path.toFile(), bindings);
    }

    @Override
    public Object eval(Path path) {
        return eval(path.toFile());
    }

    @Override
    public Object eval(String code, String name, Map<String, Object> bindings) {
        Script script = shell.parse(code, name);
        script.setBinding(new Binding(bindings));
        return script.run();
    }

    @Override
    public Object eval(String code, String name) {
        return shell.evaluate(code, name);
    }

    @Override
    public Object eval(Reader reader, String name, Map<String, Object> bindings) {
        Script script = shell.parse(reader, name);
        script.setBinding(new Binding(bindings));
        return script.run();
    }

    @Override
    public Object eval(Reader reader, String name) {
        return shell.evaluate(reader, name);
    }

    @Override
    public String replace(String source, Map<String, Object> bindings) {
        for (int i = 0; i < 16; i++) {
            try {
                Template template = templateEngine.createTemplate(source);
                Writable writable = template.make(new DelegatedMap<String, Object>(bindings) {
                    @Override
                    public Object get(Object key) {
                        return delegate.containsKey(key) ? delegate.get(key) : "#" + key;
                    }
                });
                String result;
                {
                    StringWriter stringWriter = new StringWriter();
                    writable.writeTo(stringWriter);
                    result = stringWriter.toString();
                }
                if (result.equals(source)) {
                    return result;
                } else {
                    source = result;
                }
            } catch (Exception x) {
                throw new IllegalStateException(x);
            }
        }
        return source;
    }

    @Override
    public GroovyClassLoader getClassLoader() {
        return shell.getClassLoader();
    }

    @Override
    public String getMime() {
        return "text/groovy";
    }

    @Override
    public String getExtension() {
        return "groovy";
    }

    @Override
    public String toString() {
        return "Groovy Scripting " + GroovySystem.getVersion();
    }
}
