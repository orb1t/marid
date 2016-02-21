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

package org.marid.beans;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

/**
 * @author Dmitry Ovchinnikov
 */
@SupportedAnnotationTypes({"org.marid.beans.MaridBean"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MaridBeanAnnotationProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final AtomicBoolean first = new AtomicBoolean(true);
        Formatter formatter = null;
        try {
            for (final TypeElement te : annotations) {
                for (final Element e : roundEnv.getElementsAnnotatedWith(te)) {
                    if (formatter == null) {
                        final FileObject beans = filer.createResource(CLASS_OUTPUT, "", "maridBeans.xml");
                        formatter = new Formatter(new PrintWriter(beans.openWriter()));
                    }
                    if (first.compareAndSet(true, false)) {
                        formatter.format("%s", e);
                    } else {
                        formatter.format("%n%s", e);
                    }
                }
            }
            return true;
        } catch (Exception x) {
            final StringWriter writer = new StringWriter();
            try (final PrintWriter printWriter = new PrintWriter(writer)) {
                x.printStackTrace(printWriter);
            }
            messager.printMessage(ERROR, writer.getBuffer());
            return false;
        } finally {
            if (formatter != null) {
                formatter.close();
            }
        }
    }
}
