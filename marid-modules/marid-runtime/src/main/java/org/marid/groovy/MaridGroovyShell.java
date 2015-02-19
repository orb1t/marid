package org.marid.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.marid.Marid;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
* @author Dmitry Ovchinnikov
*/
class MaridGroovyShell extends GroovyShell {

    MaridGroovyShell(ClassLoader classLoader, Binding binding, CompilerConfiguration cc) {
        super(classLoader, binding, cc);
    }

    @Override
    public Script parse(GroovyCodeSource codeSource) throws CompilationFailedException {
        final Script script = super.parse(codeSource);
        final AnnotationConfigApplicationContext context = Marid.getCurrentContext();
        if (context != null && context.isActive()) {
            context.getAutowireCapableBeanFactory().autowireBean(script);
        }
        return script;
    }

    @Override
    public Object run(GroovyCodeSource source, String[] args) throws CompilationFailedException {
        final Script script = parse(source);
        script.setProperty("args", args);
        return script.run();
    }

    @Override
    public Object run(File scriptFile, String[] args) throws CompilationFailedException, IOException {
        return run(new GroovyCodeSource(scriptFile), args);
    }

    @Override
    public Object run(Reader in, String fileName, String[] args) throws CompilationFailedException {
        return run(new GroovyCodeSource(in, fileName, DEFAULT_CODE_BASE), args);
    }
}
