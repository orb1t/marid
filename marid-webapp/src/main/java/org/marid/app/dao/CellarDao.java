/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * #L%
 */

package org.marid.app.dao;

import org.marid.app.common.Directories;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Repository
@SessionScope
public class CellarDao {

  private final Path userDir;
  private final Logger logger;

  @Autowired
  public CellarDao(Directories directories, Logger logger) {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    this.logger = logger;
    this.userDir = directories.getUsers().resolve(authentication.getName());
  }

  @PostConstruct
  private void createProfilesDirectoryIfNecessary() throws IOException {
    Files.createDirectories(userDir);

    logger.info("Session profiles: {}", userDir);
  }

  public Path getUserDir() {
    return userDir;
  }

  public NavigableSet<String> profileNames() {
    try {
      return Files.list(userDir)
          .filter(Files::isDirectory)
          .map(Path::getFileName)
          .map(Path::toString)
          .collect(Collectors.toCollection(TreeSet::new));
    } catch (IOException x) {
      throw new UncheckedIOException(x);
    }
  }
}
