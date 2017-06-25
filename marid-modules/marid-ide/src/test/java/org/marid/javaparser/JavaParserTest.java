/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.SlowTests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({SlowTests.class})
public class JavaParserTest {

    @Test
    public void parseQualified() throws IOException {
        final CompilationUnit compilationUnit;
        try (final InputStream inputStream = getClass().getResourceAsStream("Example1.java")) {
            compilationUnit = JavaParser.parse(inputStream, StandardCharsets.UTF_8);
        }
        final ClassOrInterfaceDeclaration type = compilationUnit.getTypes().stream()
                .filter(ClassOrInterfaceDeclaration.class::isInstance)
                .map(ClassOrInterfaceDeclaration.class::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No types detected"));
        final MethodDeclaration method = type.getMethods().get(0);

        assertEquals("java.lang.String", method.getType().asString());
        assertEquals("x", method.getParameter(0).getNameAsString());
        assertEquals("java.lang.String", method.getParameter(0).getType().asString());
    }
}
