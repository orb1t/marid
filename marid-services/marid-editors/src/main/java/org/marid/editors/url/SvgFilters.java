package org.marid.editors.url;

/*-
 * #%L
 * marid-editors
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

import javafx.stage.FileChooser.ExtensionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
public class SvgFilters {

    @Bean
    @Order(1)
    public ExtensionFilter svgExtensionFilter() {
        return new ExtensionFilter(s("SVG files"), "*.svg");
    }

    @Bean
    @Order(2)
    public ExtensionFilter svgzExtensionFilter() {
        return new ExtensionFilter(s("Compressed SVG files"), "*.svgz");
    }
}
