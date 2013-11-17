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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * @author Dmitry Ovchinnikov
 */
public class FileUtils {

    public static final FileVisitor<Path> CLEANER = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
    };

    public static final FileVisitor<Path> RECURSIVE_CLEANER = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    public static TreeSet<Path> listSorted(Path dir, String glob) throws IOException {
        final TreeSet<Path> set = new TreeSet<>();
        try (final DirectoryStream<Path> ds = Files.newDirectoryStream(dir, glob)) {
            for (final Path path : ds) {
                set.add(path);
            }
        }
        return set;
    }

    public static void copy(Path source, Path target) throws IOException {
        new CopyTask(source, target).call();
    }

    public static void remove(Path path) throws IOException {
        new RemoveTask(path).call();
    }

    public static class RemoveTask implements Callable<Boolean> {

        protected final Path path;

        public RemoveTask(Path path) {
            this.path = path;
        }

        @Override
        public Boolean call() throws IOException {
            if (Files.exists(path)) {
                remove(path);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        protected void remove(Path path) throws IOException {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
                    for (Path entry : ds) {
                        remove(entry);
                    }
                }
            }
            Files.delete(path);
        }
    }

    public static class CopyTask implements Callable<Boolean> {

        protected final Path source;
        protected final Path target;

        public CopyTask(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public Boolean call() throws IOException {
            if (Files.notExists(source)) {
                return Boolean.FALSE;
            }
            if (!Files.isDirectory(target)) {
                Files.createDirectories(target);
            }
            if (Files.isRegularFile(source)) {
                copy(source, target.resolve(source.getFileName()));
                return Boolean.TRUE;
            } else if (Files.isDirectory(source)) {
                copy(source, target);
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }

        protected void copy(Path src, Path dst) throws IOException {
            if (Files.isDirectory(src)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(src)) {
                    for (Path entry : ds) {
                        Path t = dst.resolve(entry.getFileName().toString());
                        if (Files.isDirectory(entry)) {
                            Files.createDirectories(t);
                        }
                        copy(entry, t);
                    }
                }
            } else {
                Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
