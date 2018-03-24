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

package org.marid.appcontext.session.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.marid.app.ivy.IvySlfLoggerAdapter;
import org.marid.appcontext.session.SessionDirectory;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class IvyConfiguration {

  @Bean
  public IvySettings ivySettings(SessionDirectory sessionDir, IvyDependencyResolvers resolvers) throws IOException {
    final Path dir = sessionDir.getDirectory().resolve("ivy");
    Files.createDirectories(dir);

    final IvySettings settings = new IvySettings();
    settings.setBaseDir(dir.toFile());
    settings.setDefaultCache(dir.resolve("cache").toFile());

    resolvers.configure(settings);

    return settings;
  }

  @Bean
  public Ivy ivy(IvySettings ivySettings, CommonProfile profile) {
    final Logger logger = LoggerFactory.getLogger("ivy:" + profile.getEmail());

    final Ivy ivy = new Ivy();
    ivy.getLoggerEngine().setDefaultLogger(new IvySlfLoggerAdapter(logger));
    ivy.setSettings(ivySettings);
    ivy.bind();

    return ivy;
  }

  @Bean
  public ResolveOptions ivyResolveOptions() {
    return new ResolveOptions()
        .setDownload(true)
        .setTransitive(true);
  }
}
