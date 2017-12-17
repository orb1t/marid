/*-
 * #%L
 * marid-ide
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

package org.marid.dependant.beaneditor;

import javafx.beans.InvalidationListener;
import org.marid.ide.project.ProjectClasses;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

@Repository
public class BeanDao {

  private final ProjectProfile profile;
  private final ProjectClasses classes;
  private final Set<Class<?>> publicClasses = new LinkedHashSet<>();
  private final AtomicBoolean dirty = new AtomicBoolean(true);
  private final InvalidationListener listener = o -> dirty.set(true);

  @Autowired
  public BeanDao(ProjectProfile profile, ProjectClasses classes) {
    this.classes = classes;
    (this.profile = profile).addListener(listener);
    listener.invalidated(profile);
  }

  public Collection<Class<?>> publicClasses() {
    if (dirty.compareAndSet(true, false)) {
      final Collection<Class<?>> classes = this.classes.classes(profile, Set.of("marid-runtime"));
      publicClasses.clear();
      publicClasses.addAll(classes);
      log(INFO, "{0} Public classes updated: {1}", profile, publicClasses.size());
    }
    return publicClasses;
  }

  @PreDestroy
  private void close() {
    profile.removeListener(listener);
  }
}
