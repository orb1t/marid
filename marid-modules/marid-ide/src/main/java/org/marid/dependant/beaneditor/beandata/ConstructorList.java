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

package org.marid.dependant.beaneditor.beandata;

import javafx.scene.control.ComboBox;
import org.marid.beans.MethodInfo;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.converter.MaridConverter;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ConstructorList extends ComboBox<MethodInfo> {

    @Autowired
    public ConstructorList(BeanData beanData, BeanInfo beanInfo, ProjectProfile profile) {
        super(beanInfo.constructors);
        setEditable(false);
        setMaxWidth(Double.MAX_VALUE);
        setConverter(new MaridConverter<>("%s"));
        setOnAction(event -> {
            final MethodInfo methodInfo = getSelectionModel().getSelectedItem();
            if (methodInfo != null) {
                beanData.beanArgs.setAll(Stream.of(methodInfo.parameters)
                        .map(p -> {
                            final BeanProp old = beanData.beanArgs.stream()
                                    .filter(a -> p.name.equals(a.getName()))
                                    .findAny()
                                    .orElse(null);
                            final BeanProp beanProp = new BeanProp();
                            beanProp.setName(old != null ? old.getName() : p.name);
                            beanProp.setType(p.type.getRawClass().getName());
                            if (old != null) {
                                beanProp.setData(old.getData());
                            }
                            return beanData;
                        })
                        .toArray(BeanProp[]::new));
            }
        });
        final Executable executable = profile.getConstructor(beanData).orElse(null);
        if (executable != null) {
            for (final MethodInfo methodInfo : getItems()) {
                final Class<?>[] ts = of(methodInfo.parameters).map(m -> m.type.getRawClass()).toArray(Class[]::new);
                if (Arrays.equals(ts, executable.getParameterTypes())) {
                    getSelectionModel().select(methodInfo);
                    break;
                }
            }
        }
    }
}
