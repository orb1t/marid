/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.dependant.valuemenu;

import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.WritableValue;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.xml.BeanFile;
import org.marid.spring.xml.DElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan
public class ValuesConfiguration extends DependantConfiguration<ValuesParams> {

    @Bean
    public WritableValue<DElement> element() {
        return param.element;
    }

    @Bean
    public ResolvableType type() {
        return param.type;
    }

    @Bean
    public ObservableStringValue name() {
        return param.name;
    }

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }

    @Bean
    public BeanFile file() {
        return param.file;
    }

    @Bean(initMethod = "run")
    public Runnable menuConfigurer(ValueMenuFiller filler) {
        return () -> filler.addTo(param.menuItems);
    }
}
