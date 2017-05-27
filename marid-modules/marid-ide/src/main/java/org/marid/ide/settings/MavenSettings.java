/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.settings;

import javafx.beans.value.WritableObjectValue;
import org.springframework.stereotype.Component;

import static org.marid.jfx.props.Props.string;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class MavenSettings extends AbstractSettings {

    public final WritableObjectValue<String> snapshotUpdatePolicy = string(preferences, "snapshotUpdatePolicy", null);
    public final WritableObjectValue<String> releaseUpdatePolicy = string(preferences, "releaseUpdatePolicy", null);
    public final WritableObjectValue<String> dependencyPluginVersion = string(preferences, "dependencyPluginVersion", "3.0.0");
    public final WritableObjectValue<String> compilerPluginVersion = string(preferences, "compilerPluginVersion", "3.6.1");
    public final WritableObjectValue<String> eclipseCompilerVersion = string(preferences, "eclipseCompilerVersion", "2.8.1");
    public final WritableObjectValue<String> jarPluginVersion = string(preferences, "jarPluginVersion", "3.0.2");
    public final WritableObjectValue<String> resourcesPluginVersion = string(preferences, "resourcesPluginVersion", "3.0.2");
    public final WritableObjectValue<String> jdtVersion = string(preferences, "jdtVersion", "3.12.3");
    public final WritableObjectValue<String> repositoryUrl = string(preferences, "repositoryUrl", "repo1.maven.apache.org/maven2/");

    @Override
    public String getName() {
        return "Maven";
    }
}
