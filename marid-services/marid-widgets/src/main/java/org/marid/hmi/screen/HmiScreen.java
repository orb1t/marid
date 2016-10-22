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

package org.marid.hmi.screen;

import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

/**
 * @author Dmitry Ovchinnikov
 */
public class HmiScreen extends BorderPane {

    private final WebView webView = new WebView();
    private final Group webGroup = new Group(webView);

    public HmiScreen() {
        setCenter(webGroup);
        webView.getEngine().setJavaScriptEnabled(true);
    }

    public void setLocation(String url) {

    }
}
