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

package org.marid.dependant.project.config.deps;

import org.apache.maven.model.Dependency;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Repository
public class RepositoryDependencies extends JdbcDaoSupport {

    private final ProjectProfile profile;

    @Autowired
    public RepositoryDependencies(DataSource dataSource, ProjectProfile profile) {
        this.profile = profile;
        setDataSource(dataSource);
    }

    private static Dependency dependency(ResultSet rs, int row) throws SQLException {
        final Dependency dependency = new Dependency();
        dependency.setGroupId(rs.getString(2));
        dependency.setArtifactId(rs.getString(1));
        dependency.setVersion(rs.getString(3));
        return dependency;
    }

    public List<Dependency> getDependencies() {
        final String sql = profile.isHmi()
                ? "select ARTIFACT_ID, GROUP_ID, VERSION from ARTIFACTS where !CONF"
                : "select ARTIFACT_ID, GROUP_ID, VERSION from ARTIFACTS where !CONF and !UI";
        return getJdbcTemplate().query(sql, RepositoryDependencies::dependency);
    }

    public List<Dependency> getConfigurationDependencies() {
        final String sql = "select ARTIFACT_ID, GROUP_ID, VERSION from ARTIFACTS where CONF";
        return getJdbcTemplate().query(sql, RepositoryDependencies::dependency);
    }
}
