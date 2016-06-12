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

import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class MavenSettings extends AbstractSettings {

    public MavenSettings() {
        super("Maven");
    }

    public String getSnapshotUpdatePolicy() {
        return getPref(String.class, "snapshotUpdatePolicy", null);
    }

    public void setSnapshotUpdatePolicy(String value) {
        putPref(String.class, "snapshotUpdatePolicy", value);
    }

    public String getReleaseUpdatePolicy() {
        return getPref(String.class, "releaseUpdatePolicy", null);
    }

    public void setReleaseUpdatePolicy(String value) {
        putPref(String.class, "releaseUpdatePolicy", value);
    }

    public String getDependencyPluginVersion() {
        return getPref("dependencyPluginVersion", "2.10");
    }

    public void setDependencyPluginVersion(String value) {
        putPref("dependencyPluginVersion", value);
    }

    public String getCompilerPluginVersion() {
        return getPref("compilerPluginVersion", "3.5.1");
    }

    public void setCompilerPluginVersion(String value) {
        putPref("compilerPluginVersion", value);
    }

    public String getEclipseCompilerVersion() {
        return getPref("eclipseCompilerVersion", "2.7");
    }

    public void setEclipseCompilerVersion(String value) {
        putPref("eclipseCompilerVersion", value);
    }

    public String getJarPluginVersion() {
        return getPref("jarPluginVersion", "2.6");
    }

    public void setJarPluginVersion(String value) {
        putPref("jarPluginVersion", value);
    }

    public String getResourcesPluginVersion() {
        return getPref("resourcesPluginVersion", "3.0.1");
    }

    public void setResourcesPluginVersion(String value) {
        putPref("resourcesPluginVersion", value);
    }
}
