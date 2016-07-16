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

package org.marid.dependant.beandata;

import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import org.controlsfx.dialog.CommandLinksDialog;
import org.controlsfx.dialog.CommandLinksDialog.CommandLinksButtonType;
import org.marid.ide.project.ProjectCacheManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.BeanData;
import org.springframework.stereotype.Component;

import java.lang.reflect.Executable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.marid.misc.Reflections.parameterName;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanDataActions {

    private final ProjectProfile profile;
    private final ProjectCacheManager cacheManager;
    private final BeanData beanData;

    public BeanDataActions(ProjectProfile profile, ProjectCacheManager cacheManager, BeanData beanData) {
        this.profile = profile;
        this.cacheManager = cacheManager;
        this.beanData = beanData;
    }

    public void onRefresh(ActionEvent event) {
        cacheManager.updateBeanData(profile, beanData);
    }

    public void onSelectConstructor(ActionEvent event) {
        final Class<?>[] types = beanData.constructorArgs.stream()
                .map(a -> profile.getClass(a.type.get()).orElse(Object.class))
                .toArray(Class<?>[]::new);
        final Map<ButtonType, Executable> executableMap = new TreeMap<>(comparing(ButtonType::getText));
        final List<CommandLinksButtonType> buttonTypes = new ArrayList<>();
        cacheManager.getConstructors(profile, beanData).forEach(e -> {
            final String text = e.getName() + "/" + e.getParameterCount();
            final String longText = Stream.of(e.getParameters())
                    .map(p -> {
                        final String type = p.getParameterizedType() instanceof Class<?>
                                ? ((Class) p.getParameterizedType()).getName()
                                : p.getParameterizedType().toString();
                        return parameterName(p) + " : " + type;
                    })
                    .collect(Collectors.joining(", "));
            final boolean def = Arrays.equals(types, e.getParameterTypes());
            final CommandLinksButtonType bt = new CommandLinksDialog.CommandLinksButtonType(text, longText, def);
            buttonTypes.add(bt);
            executableMap.put(bt.getButtonType(), e);
        });
        final CommandLinksDialog dialog = new CommandLinksDialog(buttonTypes);
        dialog.getDialogPane().setPrefWidth(1024);
        final Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            final Executable e = executableMap.get(result.get());
            if (e == null) {
                return;
            }
            cacheManager.updateBeanDataConstructorArgs(e.getParameters(), beanData);
        }
    }
}
