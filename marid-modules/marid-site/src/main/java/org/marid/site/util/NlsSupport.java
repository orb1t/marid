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

package org.marid.site.util;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.marid.l10n.L10nSupport;
import org.marid.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public interface NlsSupport extends L10nSupport {

    @Override
    default Locale getDefaultL10nLocale() {
        return RWT.getLocale();
    }

    default Image img(Device device, String path) {
        try (final InputStream inputStream = Utils.currentClassLoader().getResourceAsStream(path)) {
            return new Image(device, inputStream);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    default Image img(Device device, String path, int width, int height) {
        return new Image(device, img(device, path).getImageData().scaledTo(width, height));
    }

    @Override
    default Function<String, String> getDefaultLFunc() {
        return TranslationService::translate;
    }
}
