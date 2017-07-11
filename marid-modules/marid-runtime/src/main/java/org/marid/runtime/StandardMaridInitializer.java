/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
        final ClassLoader classLoader = context.getClassLoader();
        try (final InputStream stream = classLoader.getResourceAsStream("META-INF/marid/bean-classes.lst")) {
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
    }
}
