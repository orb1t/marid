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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Stream.concat;
import static org.marid.collections.MaridIterators.lineIterator;
import static org.marid.misc.StringUtils.pathEndsWith;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Urls {

  @NotNull
  static URL url(@NotNull String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException x) {
      throw new IllegalArgumentException(url, x);
    }
  }

  @NotNull
  static URL url(@NotNull Path path) {
    try {
      return path.toUri().toURL();
    } catch (MalformedURLException x) {
      throw new IllegalArgumentException(path.toString(), x);
    }
  }

  @NotNull
  static URI uri(@NotNull String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException x) {
      throw new IllegalArgumentException(uri, x);
    }
  }

  @NotNull
  static String urlEncode(@NotNull String text) {
    try {
      return URLEncoder.encode(text, UTF_8.name());
    } catch (UnsupportedEncodingException x) {
      throw new IllegalArgumentException(UTF_8.name(), x);
    }
  }

  @NotNull
  static String urlDecode(@NotNull String text) {
    try {
      return URLDecoder.decode(text, UTF_8.name());
    } catch (UnsupportedEncodingException x) {
      throw new IllegalArgumentException(UTF_8.name(), x);
    }
  }

  @NotNull
  static URL[] classpath(@NotNull Path path, @NotNull Path... paths) {
    final Stream<URL> pathUrls = Stream.of(paths).filter(Files::isDirectory).map(Urls::url);
    try {
      return concat(Files.list(path).filter(pathEndsWith(".jar")).map(Urls::url), pathUrls)
          .distinct()
          .toArray(URL[]::new);
    } catch (NotDirectoryException | NoSuchFileException x) {
      return new URL[0];
    } catch (IOException x) {
      throw new UncheckedIOException(x);
    }
  }

  @NotNull
  static SortedSet<Path> jars(@NotNull Path path) {
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

  @NotNull
  static SortedSet<Path> files(@NotNull Path path, @NotNull String ext) {
    try {
      return Files.find(path, 255, (p, a) -> p.getFileName().toString().endsWith("." + ext))
          .collect(Collectors.toCollection(TreeSet::new));
    } catch (NotDirectoryException | NoSuchFileException x) {
      return Collections.emptySortedSet();
    } catch (IOException x) {
      throw new UncheckedIOException(x);
    }
  }

  @NotNull
  static Stream<String> lines(@NotNull ClassLoader classLoader, @NotNull String resource) {
    return classLoader.resources(resource)
        .flatMap(url -> {
          try {
            final Scanner scanner = new Scanner(new InputStreamReader(url.openStream(), UTF_8));
            final Spliterator<String> spliterator = spliteratorUnknownSize(lineIterator(scanner), NONNULL);
            return StreamSupport.stream(spliterator, false).onClose(scanner::close);
          } catch (IOException x) {
            throw new UncheckedIOException(x);
          }
        });
  }
}
