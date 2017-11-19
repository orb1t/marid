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

package org.marid.ide.maven;

import org.marid.idelib.model.MavenArtifact;
import org.marid.jfx.action.FxAction;
import org.marid.idelib.spring.annotation.IdeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class MavenRepositoryManager implements AutoCloseable {

  @Autowired
  public MavenRepositoryManager(MavenRepositories repositories) throws Exception {
  }

  @IdeAction
  public Spliterator<FxAction> repositoryActions() {
    final LinkedList<FxAction> list = new LinkedList<>();
    return list.spliterator();
  }

  public Stream<MavenArtifact> getMaridArtifacts(String group) {
    return Stream.empty();
  }

  @Override
  public void close() throws Exception {

  }
}
