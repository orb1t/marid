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
    public final boolean conf;

    public Artifact(String groupId, String artifactId, String version, boolean conf) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
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
