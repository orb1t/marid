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
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

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

    public static String fileNameWithoutExtension(String file) {
        final Path path = Paths.get(file);
        final String name = path.getFileName().toString();
        final int p = name.lastIndexOf('.');
        return p >= 0 ? name.substring(0, p) : name;
    }

    public static String extension(String file) {
        final Path path = Paths.get(file);
        final String name = path.getFileName().toString();
        final int p = name.lastIndexOf('.');
        return p >= 0 ? name.substring(p) : name;
    }

    public static TreeSet<Path> listSorted(Path dir, String glob) throws IOException {
        final TreeSet<Path> set = new TreeSet<>();
        try (final DirectoryStream<Path> ds = Files.newDirectoryStream(dir, glob)) {
            for (final Path path : ds) {
                set.add(path);
            }
        }
        return set;
    }

    public static boolean copy(Path source, Path target) throws IOException {
        return Boolean.TRUE == new CopyTask(source, target).call();
    }

    public static boolean remove(Path path) throws IOException {
        return Boolean.TRUE == new RemoveTask(path).call();
    }

    public static void copyFromZip(Path zip, Path dest) throws IOException {
        try (final FileSystem fs = FileSystems.newFileSystem(zip.toUri(), Collections.<String, Object>emptyMap())) {
            for (final Path dir : fs.getRootDirectories()) {
                copy(dir, dest);
            }
        }
    }

    public static void moveFromZip(Path zip, Path dest) throws IOException {
        copyFromZip(zip, dest);
        Files.delete(zip);
    }

    public static void copyToZip(Path source, Path zip) throws IOException {
        try (final FileSystem fs = FileSystems.newFileSystem(zip.toUri(), Collections.singletonMap("create", "true"))) {
            final Path dest = fs.getRootDirectories().iterator().next();
            copy(source, dest);
        }
    }

    public static void toZipCopy(Path zip, Path... sources) throws IOException {
        try (final FileSystem fs = FileSystems.newFileSystem(zip.toUri(), Collections.singletonMap("create", "true"))) {
            final Path dest = fs.getRootDirectories().iterator().next();
            for (final Path source : sources) {
                if (Files.isDirectory(source)) {
                    final Path dir = dest.resolve(source.getFileName());
                    Files.createDirectory(dir);
                    copy(source, dir);
                } else {
                    copy(source, dest);
                }
            }
        }
    }

    public static void toZipMove(Path zip, Path... sources) throws IOException {
        toZipCopy(zip, sources);
        for (final Path source : sources) {
            remove(source);
        }
    }

    public static void moveToZip(Path source, Path zip) throws IOException {
        copyToZip(source, zip);
        remove(source);
    }

    public static Path getOrCreateDirectory(Path path) {
        try {
            Files.createDirectories(path);
            return path;
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    public static class PatternFileFilter implements Filter<Path> {

        private final Pattern pattern;

        public PatternFileFilter(String pattern) {
            this.pattern = pattern == null ? null : Pattern.compile(pattern);
        }

        @Override
        public boolean accept(Path entry) throws IOException {
            return pattern == null || pattern.matcher(entry.getFileName().toString()).matches();
        }
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
        protected final CopyOption[] copyOptions;

        public CopyTask(Path source, Path target, CopyOption... copyOptions) {
            this.source = source;
            this.target = target;
            this.copyOptions = copyOptions;
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
                try (final DirectoryStream<Path> ds = Files.newDirectoryStream(src)) {
                    for (final Path entry : ds) {
                        final Path t = dst.resolve(entry.getFileName().toString());
                        if (Files.isDirectory(entry)) {
                            Files.createDirectories(t);
                        }
                        copy(entry, t);
                    }
                }
            } else {
                Files.copy(src, dst, copyOptions);
            }
        }
    }
}
