package org.marid.runtime;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Scanner;

/**
 * @author Dmitry Ovchinnikov
 */
public class StandardMaridInitializer implements ApplicationContextInitializer<AnnotationConfigApplicationContext> {

    @Override
    public void initialize(AnnotationConfigApplicationContext applicationContext) {
        applicationContext.register(MaridRuntime.class);

        try {
            init(applicationContext);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        } catch (ClassNotFoundException x) {
            throw new ApplicationContextException("Unable to initialize " + applicationContext, x);
        }
    }

    private void init(AnnotationConfigApplicationContext context) throws IOException, ClassNotFoundException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream stream = classLoader.getResourceAsStream("bean-classes.lst")) {
            if (stream != null) {
                try (final Scanner scanner = new Scanner(stream, "UTF-8")) {
                    while (scanner.hasNextLine()) {
                        final String line = scanner.nextLine().trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }

                        context.register(Class.forName(line, true, classLoader));
                    }
                }
            }
        }

        try (final InputStream stream = classLoader.getResourceAsStream("bean-packages.lst")) {
            if (stream != null) {
                try (final Scanner scanner = new Scanner(stream, "UTF-8")) {
                    while (scanner.hasNextLine()) {
                        final String line = scanner.nextLine().trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        context.scan(line.substring(0, line.length() - 1));
                    }
                }
            }
        }
    }
}
