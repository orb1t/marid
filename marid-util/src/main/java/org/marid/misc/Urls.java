/*-
 * #%L
 * marid-util
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

package org.marid.misc;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Stream.concat;
import static org.marid.misc.StringUtils.pathEndsWith;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Urls {

    @Nonnull
    static URL url(@Nonnull String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException x) {
            throw new IllegalArgumentException(url, x);
        }
    }

    @Nonnull
    static URL url(@Nonnull Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException x) {
            throw new IllegalArgumentException(path.toString(), x);
        }
    }

    @Nonnull
    static URI uri(@Nonnull String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(uri, x);
        }
    }

    @Nonnull
    static String urlEncode(@Nonnull String text) {
        try {
            return URLEncoder.encode(text, UTF_8.name());
        } catch (UnsupportedEncodingException x) {
            throw new IllegalArgumentException(UTF_8.name(), x);
        }
    }

    @Nonnull
    static String urlDecode(@Nonnull String text) {
        try {
            return URLDecoder.decode(text, UTF_8.name());
        } catch (UnsupportedEncodingException x) {
            throw new IllegalArgumentException(UTF_8.name(), x);
        }
    }

    @Nonnull
    static Set<URL> classpath(@Nonnull Path path, @Nonnull Path... paths) {
        final Stream<URL> pathUrls = Stream.of(paths).filter(Files::isDirectory).map(Urls::url);
        try {
            return concat(Files.list(path).filter(pathEndsWith(".jar")).map(Urls::url), pathUrls)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (NotDirectoryException | NoSuchFileException x) {
            return Collections.emptySet();
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    @Nonnull
    static SortedSet<Path> jars(@Nonnull Path path) {
        try {
            try (final Stream<Path> stream = Files.list(path).filter(pathEndsWith(".jar"))) {
                return stream.collect(Collectors.toCollection(TreeSet::new));
            }
        } catch (NotDirectoryException | NoSuchFileException x) {
            return Collections.emptySortedSet();
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    @Nonnull
    static SortedSet<Path> files(@Nonnull Path path, @Nonnull String ext) {
        try {
            return Files.find(path, 255, (p, a) -> p.getFileName().toString().endsWith("." + ext))
                    .collect(Collectors.toCollection(TreeSet::new));
        } catch (NotDirectoryException | NoSuchFileException x) {
            return Collections.emptySortedSet();
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }
}
