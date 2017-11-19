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

package org.marid.dependant.settings;

import org.marid.ide.settings.MavenSettings;
import org.marid.jfx.panes.GenericGridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class MavenTab extends GenericGridPane implements SettingsEditor {

  private final MavenSettings pref;

  @Autowired
  public MavenTab(MavenSettings pref) {
    this.pref = pref;
    addTextField("Releases update policy by default", pref.releaseUpdatePolicy);
    addTextField("Snapshot update policy by default", pref.snapshotUpdatePolicy);
    addSeparator();
    addTextField("Dependency plugin version", pref.dependencyPluginVersion);
    addTextField("Compiler plugin version", pref.compilerPluginVersion);
    addTextField("JAR plugin version", pref.jarPluginVersion);
    addTextField("Resources plugin version", pref.resourcesPluginVersion);
    addSeparator();
    addTextField("Exec plugin version", pref.execPluginVersion);
  }

  @Override
  public MavenSettings getSettings() {
    return pref;
  }
}
