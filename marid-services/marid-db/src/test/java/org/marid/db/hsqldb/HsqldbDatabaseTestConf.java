/*-
 * #%L
 * marid-db
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
