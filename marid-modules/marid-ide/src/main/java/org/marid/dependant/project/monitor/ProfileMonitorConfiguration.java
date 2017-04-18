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

package org.marid.dependant.project.monitor;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.dependant.project.ProjectParams;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
@EnableScheduling
@Import({ProfileObjectTree.class})
public class ProfileMonitorConfiguration extends DependantConfiguration<ProjectParams> {

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }

    @Bean(initMethod = "show")
    public Stage stage(ProfileObjectTree tree, ProjectProfile profile) {
        final Stage stage = new Stage();
        stage.initOwner(Ide.primaryStage);
        stage.setScene(new Scene(tree, 1200, 800));
        stage.setTitle(profile.getName());
        return stage;
    }
}
