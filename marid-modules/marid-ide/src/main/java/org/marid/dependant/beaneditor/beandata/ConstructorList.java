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
import org.marid.spring.xml.BeanArg;
import org.marid.spring.xml.BeanData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ConstructorList extends ComboBox<Executable> {

    @Autowired
    public ConstructorList(BeanData data) {
        super(data.constructors);
        setEditable(false);
        setMaxWidth(Double.MAX_VALUE);
        setConverter(new MaridConverter<>(this::format));
        setOnAction(event -> ofNullable(getSelectionModel().getSelectedItem()).ifPresent(c -> {
            final BeanArg[] args = Stream.of(c.getParameters()).map(p -> data.beanArgs.stream()
                    .filter(a -> p.getName().equals(a.getName()))
                    .peek(a -> a.setType(p.getType().getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        final BeanArg beanArg = new BeanArg();
                        beanArg.setName(p.getName());
                        beanArg.setType(p.getType().getName());
                        return beanArg;
                    }))
                    .toArray(BeanArg[]::new);
            data.beanArgs.setAll(args);
        }));
    }

    @Autowired
    private void init(ProjectProfile profile, BeanData beanData) {
        profile.updateBeanData(beanData);
        profile.getConstructor(beanData).ifPresent(c -> {
            for (final Executable executable : getItems()) {
                if (Arrays.equals(c.getParameterTypes(), executable.getParameterTypes())) {
                    getSelectionModel().select(executable);
                }
            }
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
}
