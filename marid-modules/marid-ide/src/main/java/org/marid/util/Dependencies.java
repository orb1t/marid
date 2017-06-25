package org.marid.util;

import org.apache.maven.model.Dependency;

import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface Dependencies {

    static boolean equals(Dependency d1, Dependency d2) {
        if (!Objects.equals(d1.getGroupId(), d2.getGroupId())) {
            return false;
        }
        if (!Objects.equals(d1.getArtifactId(), d2.getArtifactId())) {
            return false;
        }
        if (!Objects.equals(d1.getVersion(), d2.getVersion())) {
            return false;
        }
        return true;
    }
}
