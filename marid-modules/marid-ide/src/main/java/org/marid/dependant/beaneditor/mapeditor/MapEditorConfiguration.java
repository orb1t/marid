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

package org.marid.dependant.beaneditor.mapeditor;

import javafx.beans.value.ObservableStringValue;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.xml.DMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
@Import({MapEditorTable.class, MapEditorTab.class})
public class MapEditorConfiguration extends DependantConfiguration<MapEditorParams> {

    @Autowired
    public MapEditorConfiguration(Environment environment) {
        super(environment);
    }

    @Bean
    public ResolvableType keyType() {
        return param().keyType;
    }

    @Bean
    public ResolvableType valueType() {
        return param().valueType;
    }

    @Bean
    public ObservableStringValue name() {
        return param().name;
    }

    @Bean
    public DMap map() {
        return param().map;
    }
}
