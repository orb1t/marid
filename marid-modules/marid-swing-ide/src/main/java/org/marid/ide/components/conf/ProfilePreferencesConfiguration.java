/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.ide.components.conf;

import org.marid.dyn.MetaInfo;
import org.marid.swing.ComponentConfiguration;
import org.marid.swing.forms.Tab;
import org.marid.swing.input.StringInputControl;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@MetaInfo(name = "Profile Preferences")
@Component
@Tab(node = "common", label = "Common", order = 1)
@Tab(node = "org", label = "Organization", order = 2)
@Tab(node = "deps", label = "Dependencies", order = 3)
@Tab(node = "build", label = "Build", order = 4)
public class ProfilePreferencesConfiguration extends ComponentConfiguration {

    /*
     * Common Tab
     */

    @MetaInfo(order = 1, group = "common", name = "Group ID")
    public final P<String> groupId = p("groupId", StringInputControl::new, () -> "mygroup");

    @MetaInfo(order = 2, group = "common", name = "Artifact ID")
    public final P<String> artifactId = p("artifactId", StringInputControl::new, () -> "myartifact");

    @MetaInfo(order = 3, group = "common", name = "Version")
    public final P<String> version = p("version", StringInputControl::new, () -> "1.0");

    @MetaInfo(order = 4, group = "common", name = "Name")
    public final P<String> name = p("name", StringInputControl::new, () -> "My Artifact");

    @MetaInfo(order = 5, group = "common", name = "Description")
    public final P<String> description = p("description", StringInputControl::new, () -> "");

    @MetaInfo(order = 6, group = "common", name = "URL")
    public final P<String> url = p("url", StringInputControl::new, () -> "");

    /*
     * Organization Tab
     */

    @MetaInfo(order = 1, group = "org", name = "Organization")
    public final P<String> orgName = p("org", StringInputControl::new, () -> "My organization");

    @MetaInfo(order = 2, group = "org", name = "Organization site")
    public final P<String> orgUrl = p("orgUrl", StringInputControl::new, () -> "");
}
