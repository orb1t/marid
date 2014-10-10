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

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.marid.concurrent.MaridTimerTask;
import org.marid.logging.LogSupport;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ImageCache implements LogSupport {

    private final ConcurrentMap<ImageKey, Image> cache = new ConcurrentHashMap<>();
    private final Timer timer = new Timer();

    @PostConstruct
    protected void init() {
        timer.schedule(new MaridTimerTask(() -> {
            for (final Map.Entry<ImageKey, Image> e : cache.entrySet()) {
                try {
                    final URI uri = new URI(e.getKey().path);
                    if (uri.isAbsolute()) {
                        final long dt = System.currentTimeMillis() - e.getKey().time;
                        if (dt > TimeUnit.MINUTES.toMillis(1L)) {
                            cache.remove(e.getKey());
                        }
                    }
                } catch (Exception x) {
                    warning("Unable to process {0}", x, e);
                }
            }
        }), 1_000L, 1_000L);
    }

    @PreDestroy
    protected void destroy() {
        timer.cancel();
    }

    public Image getImage(Device device, String path, int width, int height) {
        final ImageKey imageKey = new ImageKey(Objects.requireNonNull(path), width, height);
        return cache.computeIfAbsent(imageKey, k -> {
            try {
                final URI uri = new URI(k.path);
                if (uri.isAbsolute()) {
                    try (final InputStream inputStream = uri.toURL().openStream()) {
                        final Image image = new Image(device, inputStream);
                        if (width < 0 || height < 0) {
                            return image;
                        } else {
                            return new Image(device, image.getImageData().scaledTo(width, height));
                        }
                    }
                } else {
                    return new Image(device, k.path);
                }
            } catch (Exception x) {
                warning("Unable to load {0}", x, path);
                return new Image(device, width < 0 ? 0 : width, height < 0 ? 0 : height);
            }
        });
    }

    public Image getImage(Device device, String path) {
        return getImage(device, path, -1, -1);
    }

    private static class ImageKey {

        private final String path;
        private final int width;
        private final int height;
        private final long time = System.currentTimeMillis();

        private ImageKey(String path, int width, int height) {
            this.path = path;
            this.width = width;
            this.height = height;
        }

        @Override
        public int hashCode() {
            return path.hashCode() ^ width ^ height;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ImageKey) {
                final ImageKey that = (ImageKey) obj;
                return this.path.equals(that.path) && this.width == that.width && this.height == that.height;
            } else {
                return false;
            }
        }
    }
}
