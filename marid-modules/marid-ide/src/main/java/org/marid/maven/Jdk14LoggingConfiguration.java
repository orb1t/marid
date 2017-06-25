package org.marid.maven;

import org.apache.maven.cli.logging.Slf4jConfiguration;

/**
 * @author Dmitry Ovchinnikov
 */
public class Jdk14LoggingConfiguration implements Slf4jConfiguration {
    @Override
    public void setRootLoggerLevel(Level level) {
    }

    @Override
    public void activate() {
    }
}
