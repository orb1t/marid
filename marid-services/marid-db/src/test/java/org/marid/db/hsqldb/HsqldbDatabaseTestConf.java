package org.marid.db.hsqldb;

import org.marid.db.dao.NumericWriter;
import org.marid.misc.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
class HsqldbDatabaseTestConf {

    @Bean
    public Path directory() throws IOException {
        return Files.createTempDirectory("hsqldb");
    }

    @Bean
    public Closeable directoryCleaner(Path directory) throws IOException {
        return () -> FileSystemUtils.deleteRecursively(directory.toFile());
    }

    @Bean
    public HsqldbProperties hsqldbProperties(Path directory) {
        return new Builder<>(new HsqldbProperties())
                .set(HsqldbProperties::setDirectory, directory.toFile())
                .build();
    }

    @Bean
    public HsqldbDatabase wrapper(HsqldbProperties hsqldbProperties) throws MalformedURLException {
        return new HsqldbDatabase(hsqldbProperties);
    }

    @Bean
    public NumericWriter numericWriter(HsqldbDatabase wrapper) throws IOException {
        return new HsqldbDaqNumericWriter(wrapper.dataSource("NUMERICS"), "NUMERICS");
    }
}
