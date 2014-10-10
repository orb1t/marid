/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.site.config;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.marid.concurrent.MaridTimerTask;
import org.marid.logging.LogSupport;
import org.marid.site.util.NlsSupport;
import org.marid.site.util.SwtSupport;
import org.marid.site.widgets.ImagePreview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectSummary implements LogSupport, NlsSupport, SwtSupport {

    private final Timer timer = new Timer();
    private volatile JsonObject jsonObject = new JsonObject();
    private final ImageCache imageCache;

    @Autowired
    public ProjectSummary(ImageCache imageCache) {
        this.imageCache = imageCache;
    }

    @PostConstruct
    public void init() {
        final MaridTimerTask task = new MaridTimerTask(() -> {
            try {
                final URL url = new URL("http://sourceforge.net/rest/p/marid");
                try (final Reader reader = new InputStreamReader(url.openStream(), UTF_8)) {
                    jsonObject = JsonObject.readFrom(reader);
                }
            } catch (Exception x) {
                warning("Unable to get {0}", x, getClass().getSimpleName());
            }
        });
        timer.schedule(task, TimeUnit.SECONDS.toMillis(1L), TimeUnit.MINUTES.toMillis(1L));
    }

    @PreDestroy
    public void destroy() {
        timer.cancel();
    }

    public JsonObject getProjectSummary() {
        return jsonObject;
    }

    public void fill(Composite composite) {
        final JsonObject object = jsonObject;
        if (!object.isEmpty()) {
            fill(composite, object);
        }
    }

    private void fill(Composite composite, JsonObject object) {
        addTitle(composite, "Project summary");
        final Composite tableComposite = new Composite(composite, SWT.NONE);
        tableComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final TableViewer tableViewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.FULL_SELECTION);
        tableViewer.getTable().setLinesVisible(true);
        tableViewer.getTable().setHeaderVisible(true);
        final TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
        nameColumn.getColumn().setText(s("Property"));
        final TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.RIGHT);
        valueColumn.getColumn().setText(s("Value"));
        nameColumn.getColumn().pack();
        valueColumn.getColumn().pack();
        final TableColumnLayout tableLayout = new TableColumnLayout();
        tableComposite.setLayout(tableLayout);
        tableLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(60, true));
        tableLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(40, true));
        addTextRow(tableViewer.getTable(), "Status", "status", object, true);
        addTextRow(tableViewer.getTable(), "Preferred support tool", "preferred_support_tool", object, false);
        addCheckRow(tableViewer.getTable(), "Private", "private", object);
        addDateRow(tableViewer.getTable(), "Creation date", "creation_date", object);

        final JsonArray screenshotsArray = object.get("screenshots").asArray();
        if (screenshotsArray != null && !screenshotsArray.isEmpty()) {
            addTitle(composite, "Screenshots");
            final Composite screenshots = new Composite(composite, SWT.NONE);
            screenshots.setLayout(new GridLayout(screenshotsArray.size(), true));
            screenshotsArray.values().stream().map(JsonValue::asObject).forEach(v -> {
                final Image fullImage = imageCache.getImage(composite.getDisplay(), v.get("url").asString());
                final Image image = imageCache.getImage(composite.getDisplay(), v.get("url").asString(), 128, 128);
                new ImagePreview(screenshots, SWT.NONE, fullImage, image);
            });
        }
    }
}
