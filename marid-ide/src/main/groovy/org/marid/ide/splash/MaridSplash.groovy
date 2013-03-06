/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide.splash

import java.awt.*

import static java.awt.RenderingHints.*

/**
 * Marid splash.
 *
 * @author Dmitry Ovchinnikov 
 */
class MaridSplash {

    private final SplashScreen splashScreen;
    private final Graphics2D graphics;
    private final Dimension size;

    MaridSplash() {
        splashScreen = SplashScreen.splashScreen;
        if (splashScreen != null) {
            size = splashScreen.size;
            synchronized (SplashScreen) {
                graphics = this.splashScreen.createGraphics();
                graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                graphics.color = Color.BLACK;
                graphics.font = new Font(Font.DIALOG_INPUT, Font.BOLD, 20);
                graphics.drawString("Marid IDE".ls(), 30, 30);
                graphics.color = new Color(0, 0, 255, 128);
                graphics.fillRect(0, 40, (int)size.width, 60);
                graphics.font = new Font(Font.SERIF, Font.PLAIN, 13);
                graphics.color = Color.BLACK;
                graphics.drawString("Free data acquisition & visualization software".ls(), 30, 60);
                graphics.drawString("Version: %s".ls("1.0"), 30, 75);
                graphics.drawString("\u00A9 2013 Marid software", 30, 90);
                splashScreen.update();
                graphics.color = Color.WHITE;
            }
        } else {
            graphics = null;
        }
    }

    boolean isSupported() {
        return splashScreen != null;
    }

    boolean isVisible() {
        synchronized (SplashScreen) {
            return splashScreen.visible;
        }
    }
}
