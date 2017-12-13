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

import org.marid.ide.project.ProjectFileType;
import org.marid.ide.project.ProjectProfile;
import org.marid.idefx.beans.IdeBean;
import org.marid.io.Xmls;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

@Configuration
@ComponentScan
public class BeanConfiguration {

  private final ProjectProfile profile;

  public BeanConfiguration(ProjectProfile profile) {
    this.profile = profile;
  }

  @Bean
  public ProjectProfile profile() {
    return profile;
  }

  @Bean
  public IdeBean root() {
    final Path beansFile = profile.get(ProjectFileType.BEANS_XML);
    if (Files.isRegularFile(beansFile)) {
      try {
        return Xmls.read(beansFile, IdeBean::new);
      } catch (Exception x) {
        log(WARNING, "Unable to read {0}", x, beansFile);
      }
    }
    return new IdeBean();
  }

  @Override
  public int hashCode() {
    return profile.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this || (obj instanceof BeanConfiguration && ((BeanConfiguration) obj).profile.equals(profile));
  }
}
