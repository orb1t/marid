/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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

package org.marid.app.maven;

import org.apache.ivy.core.settings.IvySettings;
import org.marid.app.spring.LoggingPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.marid.test.TestGroups.MANUAL;

@ContextConfiguration(classes = {Context.class})
public class RepositoryTest extends AbstractTestNGSpringContextTests {

  @Test(groups = {MANUAL})
  public void testResolve() {

  }
}

@Configuration
@Import({LoggingPostProcessor.class})
class Context {

  @Bean
  public IvySettings settings() {
    final IvySettings settings = new IvySettings();
    return settings;
  }
}
