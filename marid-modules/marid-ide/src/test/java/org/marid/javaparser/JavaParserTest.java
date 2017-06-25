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
