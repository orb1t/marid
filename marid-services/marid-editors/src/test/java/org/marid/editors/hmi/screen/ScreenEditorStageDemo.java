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

package org.marid.editors.hmi.screen;

import javafx.application.Application;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.marid.ide.project.ProfileInfo;
import org.marid.spring.beandata.BeanEditorContext;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;

import java.io.File;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Ovchinnikov
 */
public class ScreenEditorStageDemo extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Preferences preferences = Preferences.userRoot().node("demo").node("svg");
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(preferences.get("dir", System.getProperty("user.home"))));
        final File directory = directoryChooser.showDialog(primaryStage);
        if (directory == null) {
            return;
        }
        preferences.put("dir", directory.getAbsolutePath());

        final Path userHome = directory.toPath();
        final BeanEditorContext context = mock(BeanEditorContext.class);
        final BeanData beanData = new BeanData();
        final BeanProp relativeLocationProp = DemoUtils.prop(beanData, "relativeLocation", String.class, null);
        final BeanProp prefWidthProp = DemoUtils.prop(beanData, "prefWidth", double.class, null);
        final BeanProp prefHeightProp = DemoUtils.prop(beanData, "prefHeight", double.class, null);

        final ProfileInfo profileInfo = mock(ProfileInfo.class);
        when(profileInfo.getSrcMainResources()).thenReturn(userHome);

        when(context.getBeanData()).thenReturn(beanData);
        when(context.getPrimaryStage()).thenReturn(primaryStage);
        when(context.getProfileInfo()).thenReturn(profileInfo);

        final ScreenEditorStage screenEditorStage = new ScreenEditorStage(context);
        primaryStage.setScene(screenEditorStage.getScene());
        primaryStage.setOnCloseRequest(event -> {
            System.out.printf("Relative location: %s%n", relativeLocationProp.getData());
            System.out.printf("Pref width: %s%n", prefWidthProp.getData());
            System.out.printf("Pref height: %s%n", prefHeightProp.getData());
        });
        primaryStage.show();
    }
}
