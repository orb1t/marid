package org.apache.maven.cli;

import org.codehaus.plexus.classworlds.ClassWorld;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridMavenCliRequest extends CliRequest {

    public MaridMavenCliRequest(String[] args, ClassWorld classWorld) {
        super(args, classWorld);
    }

    public MaridMavenCliRequest directory(Path directory) {
        workingDirectory = directory.toAbsolutePath().toString();
        multiModuleProjectDirectory = directory.toFile();
        return this;
    }
}
