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
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.converter.MaridConverter;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.marid.util.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ConstructorList extends ComboBox<Executable> {

    private final BeanData beanData;
    private final ProjectProfile profile;

    @Autowired
    public ConstructorList(BeanData data, ProjectProfile profile) {
        this.beanData = data;
        this.profile = profile;
        setEditable(false);
        setMaxWidth(Double.MAX_VALUE);
        setConverter(new MaridConverter<>(this::format));
        setOnAction(event -> {
            final Executable constructor = getSelectionModel().getSelectedItem();
            if (constructor == null) {
                return;
            }
            final BeanProp[] props = Stream.of(constructor.getParameters())
                    .map(p -> {
                        final BeanProp beanProp = new BeanProp();
                        data.beanArgs.stream()
                                .filter(a -> Reflections.parameterName(p).equals(a.getName()))
                                .peek(a -> {
                                    beanProp.setData(a.getData());
                                    beanProp.setName(a.getName());
                                })
                                .findAny()
                                .orElseGet(() -> {
                                    beanProp.setName(p.getName());
                                    return null;
                                });
                        return data;
                    })
                    .toArray(BeanProp[]::new);
            data.beanArgs.setAll(props);
        });
    }

    private String format(Executable executable) {
        final Parameter[] parameters = executable.getParameters();
        if (parameters.length == 0) {
            return "()";
        } else {
            return Stream.of(parameters).map(p -> p.getName() + ": " + type(p)).collect(joining(", "));
        }
    }

    private String type(Parameter parameter) {
        if (parameter.getType().isPrimitive()) {
            return parameter.getType().getName();
        } else {
            final ResolvableType resolvableType = ResolvableType.forType(parameter.getParameterizedType());
            if (resolvableType.hasGenerics()) {
                return resolvableType.toString();
            } else {
                if (isSimpleClass(parameter.getType())) {
                    return parameter.getType().getSimpleName();
                } else {
                    return resolvableType.toString();
                }
            }
        }
    }

    private boolean isSimpleClass(Class<?> type) {
        if (type.getPackage() == null) {
            return true;
        }
        switch (type.getPackage().getName()) {
            case "java.lang":
            case "java.net":
            case "java.util":
                return true;
            default:
                return false;
        }
    }

    @PostConstruct
    public void update() {
        getItems().setAll(profile.getConstructors(beanData).toArray(Executable[]::new));
        final Executable executable = profile.getConstructor(beanData).orElse(null);
        if (executable != null) {
            getSelectionModel().select(executable);
        }
    }
}
