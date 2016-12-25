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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javax.annotation.PreDestroy;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ConstructorList extends ComboBox<Executable> implements InvalidationListener {

    private final BeanData beanData;
    private final ProjectProfile profile;

    @Autowired
    public ConstructorList(BeanData beanData, ProjectProfile profile) {
        this.beanData = beanData;
        this.profile = profile;
        setEditable(false);
        setMaxWidth(Double.MAX_VALUE);
        setConverter(new MaridConverter<>(this::format));
        setOnAction(event -> {
            final Executable constructor = getSelectionModel().getSelectedItem();
            if (constructor != null) {
                beanData.beanArgs.setAll(Stream.of(constructor.getParameters())
                        .map(p -> {
                            final BeanProp old = beanData.beanArgs.stream()
                                    .filter(a -> Reflections.parameterName(p).equals(a.getName()))
                                    .findAny()
                                    .orElse(null);
                            final BeanProp beanProp = new BeanProp();
                            beanProp.setName(old != null ? old.getName() : p.getName());
                            beanProp.setType(p.getType().getName());
                            if (old != null) {
                                beanProp.setData(old.getData());
                            }
                            return beanData;
                        })
                        .toArray(BeanProp[]::new));
            }
        });
    }

    @PostConstruct
    private void init() {
        invalidated(profile);
        profile.addListener(this);
    }

    @PreDestroy
    private void destroy() {
        profile.removeListener(this);
    }

    private String format(Executable executable) {
        return Stream.of(executable.getParameters()).map(p -> p.getName() + ": " + type(p)).collect(joining(", "));
    }

    private String type(Parameter parameter) {
        if (parameter.getType().isPrimitive()) {
            return parameter.getType().getName();
        } else if (parameter.getType().getPackage() == null) {
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
        return type.getPackage().getName().matches("java[.](util|net)[.].+");
    }

    @Override
    public void invalidated(Observable observable) {
        getItems().setAll(profile.getConstructors(beanData).toArray(Executable[]::new));
        final Executable executable = profile.getConstructor(beanData).orElse(null);
        if (executable != null) {
            getSelectionModel().select(executable);
        }
    }
}
