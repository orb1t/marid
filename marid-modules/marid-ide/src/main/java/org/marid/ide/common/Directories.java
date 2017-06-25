package org.marid.ide.common;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class Directories {

    private final Path userHome;
    private final Path marid;
    private final Path profiles;
    private final Path repo;

    public Directories() {
        userHome = Paths.get(System.getProperty("user.home"));
        marid = userHome.resolve("marid");
        profiles = marid.resolve("profiles");
        repo = marid.resolve("repo");
    }

    @PostConstruct
    private void init() throws IOException {
        Files.createDirectories(profiles);
        Files.createDirectories(repo);

        System.setProperty("maven.repo.local", repo.toAbsolutePath().toString());
    }

    public Path getUserHome() {
        return userHome;
    }

    public Path getMarid() {
        return marid;
    }

    public Path getProfiles() {
        return profiles;
    }

    public Path getRepo() {
        return repo;
    }
}
