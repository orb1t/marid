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

package org.marid.runtime.annotation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.NOTE;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

/**
 * @author Dmitry Ovchinnikov
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"org.marid.runtime.annotation.MaridBean"})
public class MaridAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MaridBean.class);
        if (!elements.isEmpty()) {
            try {
                final FileObject file = processingEnv
                        .getFiler()
                        .createResource(CLASS_OUTPUT, "", "META-INF/marid/bean-classes.lst");
                try (final BufferedWriter writer = new BufferedWriter(file.openWriter())) {
                    for (final Element element : elements) {
                        writer.write(element.toString());
                        writer.newLine();
                        processingEnv.getMessager().printMessage(NOTE, "Bean found", element);
                    }
                }
            } catch (IOException x) {
                throw new UncheckedIOException(x);
            }
        }
        return false;
    }
}
