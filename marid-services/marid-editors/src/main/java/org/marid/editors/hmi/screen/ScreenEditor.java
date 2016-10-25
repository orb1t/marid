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

import javafx.stage.Stage;
import org.marid.hmi.screen.HmiScreen;
import org.marid.spring.beandata.BeanEditor;
import org.marid.spring.beandata.BeanEditorContext;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.ref.DRef;

import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
public class ScreenEditor implements BeanEditor {

    private BeanData sceneBeanData;
    private BeanData rootBeanData;

    @Override
    public boolean isCompatibe(BeanEditorContext beanEditorContext) {
        if (!Stage.class.isAssignableFrom(beanEditorContext.getType())) {
            return false;
        }
        final String sceneBeanName = beanEditorContext.getBeanData().property("scene")
                .filter(p -> p.data.get() instanceof DRef)
                .map(p -> DRef.class.cast(p.data.get()))
                .map(DRef::getBean)
                .orElse(null);
        if (sceneBeanName == null) {
            return false;
        }
        sceneBeanData = beanEditorContext.getProfileInfo().findBean(sceneBeanName).orElse(null);
        if (sceneBeanData == null) {
            return false;
        }
        final String rootBeanName = sceneBeanData.arg("root")
                .filter(a -> a.data.get() instanceof DRef)
                .map(a -> DRef.class.cast(a.data.get()))
                .map(DRef::getBean)
                .orElse(null);
        if (rootBeanName == null) {
            return false;
        }
        rootBeanData = beanEditorContext.getProfileInfo().findBean(rootBeanName).orElse(null);
        if (rootBeanData == null) {
            return false;
        }
        final Optional<Class<?>> rootBeanDataType = beanEditorContext.getProfileInfo().getClass(rootBeanData);
        return rootBeanDataType.isPresent() && HmiScreen.class.isAssignableFrom(rootBeanDataType.get());
    }

    @Override
    public String getName() {
        return "Screen Editor";
    }

    @Override
    public void run(BeanEditorContext context) {

    }
}
