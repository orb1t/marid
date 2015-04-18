/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.bd;

import groovy.inspect.swingui.AstNodeToScriptVisitor;
import org.codehaus.groovy.ast.ClassNode;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;

/**
 * @author Dmitry Ovchinnikov
 */
public class ClassHelper {

    public static void saveClassNode(Path directory, ClassNode classNode) throws IOException {
        try (final Writer writer = newBufferedWriter(directory.resolve(classNode.getName()), UTF_8)) {
            final AstNodeToScriptVisitor visitor = new AstNodeToScriptVisitor(writer);
            visitor.visitClass(classNode);
        }
    }
}
