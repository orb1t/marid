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

package org.marid.ide.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.maven.model.Dependency;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public final class Artifact {

    public final String groupId;
    public final String artifactId;
    public final String version;
    public final boolean hmi;
    public final boolean conf;

    public Artifact(String groupId, String artifactId, String version, boolean hmi, boolean conf) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.hmi = hmi;
        this.conf = conf;
    }

    public Dependency toDependency() {
        final Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        return dependency;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
