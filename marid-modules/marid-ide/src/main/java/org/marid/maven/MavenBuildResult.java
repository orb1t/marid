package org.marid.maven;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
public class MavenBuildResult {

    public final long time;
    public final List<Throwable> exceptions;

    public MavenBuildResult(long time, List<Throwable> exceptions) {
        this.time = time;
        this.exceptions = exceptions;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
