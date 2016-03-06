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

import org.marid.xml.XmlBind;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.xml.bind.Marshaller;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;
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
        try {
            final MaridBeansXml beansXml = new MaridBeansXml();
            for (final TypeElement te : annotations) {
                for (final Element e : roundEnv.getElementsAnnotatedWith(te)) {
                    final MaridBean maridBean = e.getAnnotation(MaridBean.class);
                    final MaridBeanXml maridBeanXml = new MaridBeanXml(maridBean);
                    maridBeanXml.kind = e.getKind();
                    if (e.getEnclosingElement() != null) {
                        maridBeanXml.parent = e.getEnclosingElement().toString();
                    }
                    switch (e.getKind()) {
                        case METHOD:
                            maridBeanXml.type = ((ExecutableElement) e).getReturnType().toString();
                            break;
                        default:
                            maridBeanXml.type = e.toString();
                            break;
                    }
                    beansXml.beans.add(maridBeanXml);
                    messager.printMessage(NOTE, "Added bean " + maridBeanXml);
                }
            }
            if (!beansXml.beans.isEmpty()) {
                final FileObject beans = filer.createResource(CLASS_OUTPUT, "", "maridBeans.xml");
                try (final OutputStream outputStream = beans.openOutputStream()) {
                    XmlBind.save(beansXml, outputStream, Marshaller::marshal);
                    messager.printMessage(NOTE, "Beans saved");
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
        }
    }
}
