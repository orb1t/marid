package org.marid.dependant.project;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.marid.ide.project.ProjectProfile;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ProjectParams {

    public final ProjectProfile profile;

    public ProjectParams(ProjectProfile profile) {
        this.profile = profile;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
