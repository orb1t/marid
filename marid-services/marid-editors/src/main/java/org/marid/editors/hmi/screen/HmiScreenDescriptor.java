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

import org.marid.beans.Info;
import org.marid.editors.url.RelativeUrlEditor;
import org.marid.editors.url.SvgFilters;
import org.marid.hmi.screen.HmiScreen;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Info(title = "HMI Screen", icon = "org/marid/hmi/screen/screen32.png")
public class HmiScreenDescriptor extends HmiScreen {

    @Info(description = "Sets a relative URL of a SVG file", editors = {RelativeUrlEditor.class, SvgFilters.class})
    @Override
    public void setRelativeUrl(String url) {
        super.setRelativeUrl(url);
    }
}
