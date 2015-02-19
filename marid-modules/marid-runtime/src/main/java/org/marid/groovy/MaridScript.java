/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.marid.Marid;
import org.marid.logging.LogSupport;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class MaridScript extends Script implements LogSupport {

    public MaridScript() {
        this(new Binding());
    }

    public MaridScript(Binding binding) {
        super(binding);
    }

    @Override
    public Object evaluate(String expression) throws CompilationFailedException {
        return evaluate(new GroovyCodeSource(expression, "script", GroovyShell.DEFAULT_CODE_BASE), null);
    }

    @Override
    public Object evaluate(File file) throws CompilationFailedException, IOException {
        return evaluate(new GroovyCodeSource(file), null);
    }

    @Override
    public void run(File file, String[] arguments) throws CompilationFailedException, IOException {
        evaluate(new GroovyCodeSource(file), arguments);
    }

    protected Object evaluate(GroovyCodeSource codeSource, String[] arguments) {
        final GroovyClassLoader classLoader = getClass().getClassLoader() instanceof GroovyClassLoader
                ? (GroovyClassLoader) getClass().getClassLoader()
                : GroovyRuntime.CLASS_LOADER;
        final Class<?> scriptClass = classLoader.parseClass(codeSource, false);
        final Script script = InvokerHelper.createScript(scriptClass, new Binding(getBinding().getVariables()));
        final AnnotationConfigApplicationContext context = Marid.getCurrentContext();
        if (context != null && context.isActive()) {
            context.getAutowireCapableBeanFactory().autowireBean(script);
        }
        if (arguments != null) {
            script.setProperty("args", arguments);
        }
        return script.run();
    }
}
