/*-
 * #%L
 * marid-ide
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

package org.marid.idelib.splash;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.marid.image.MaridIconFx;

public class MaridPreloader extends Preloader {

  private final MaridSplash splash = new MaridSplash();

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Marid");
    primaryStage.getIcons().addAll(MaridIconFx.getIcons(24, 32));
    primaryStage.setScene(new Scene(splash));
    primaryStage.show();
  }

  @Override
  public void handleApplicationNotification(PreloaderNotification info) {
    if (info instanceof MaridSplashCloseNotification) {
      splash.close();
    }
  }
}
