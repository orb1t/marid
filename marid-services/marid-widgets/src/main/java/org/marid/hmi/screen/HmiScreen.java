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

import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
public class HmiScreen extends BorderPane {

    private final WebView webView;

    public HmiScreen() {
        setCenter(webView = new WebView());
        webView.getEngine().setJavaScriptEnabled(true);
    }

    public void setRelativeUrl(String url) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (final InputStream is = requireNonNull(classLoader.getResourceAsStream(url))) {
            try (final Scanner scanner = new Scanner(is, "UTF-8")) {
                final StringBuilder builder = new StringBuilder();
                while (scanner.hasNextLine()) {
                    builder.append(scanner.nextLine());
                    builder.append('\n');
                }
                webView.getEngine().loadContent(builder.toString(), "image/svg+xml");
            }
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public double getZoom() {
        return webView.getZoom();
    }

    public void setZoom(double zoom) {
        webView.setZoom(zoom);
    }

    public void setLocation(String url) {
        webView.getEngine().load(url);
    }

    public WebView getWebView() {
        return webView;
    }
}
