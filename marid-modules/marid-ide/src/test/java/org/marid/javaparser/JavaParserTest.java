/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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
