/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.nio;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Dmitry Ovchinnikov
 */
public class FileUtils {

    public static void copyDir(Path source, Path dest) throws IOException {
        if (!Files.isDirectory(dest)) {
            Files.createDirectories(dest);
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(source)) {
            for (Path entry : ds)  {
                Path target = dest.resolve(entry.getFileName());
                if (Files.isDirectory(entry)) {
                    copyDir(entry, target);
                } else {
                    Files.copy(entry, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public static void removeDir(Path path) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            for (Path entry : ds) {
                if (Files.isDirectory(entry)) {
                    removeDir(entry);
                } else {
                    Files.delete(entry);
                }
            }
        }
        Files.delete(path);
    }
}
