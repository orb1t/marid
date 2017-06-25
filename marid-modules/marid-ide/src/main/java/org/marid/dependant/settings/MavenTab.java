package org.marid.dependant.settings;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    }

    @Override
    public MavenSettings getSettings() {
        return pref;
    }
}
