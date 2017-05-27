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

package org.marid.dependant.beantree.data;

import javafx.beans.value.ObservableValue;
import org.marid.spring.xml.*;

import java.util.Optional;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ItemTextFactory {

    static String text(DRef ref) {
        return ref.getBean();
    }

    static String text(DValue value) {
        return value.getValue();
    }

    static String text(BeanData beanData) {
        return beanData.getName();
    }

    static String text(DElementHolder DElementHolder) {
        return DElementHolder.getName();
    }

    static String text(DCollection collection) {
        return "" + collection.elements.size();
    }

    static String text(ObservableValue<DElement> element) {
        return Optional.ofNullable(element.getValue())
                .flatMap(e -> Optional.ofNullable(findMethod(ItemTextFactory.class, "text", e.getClass()))
                        .map(m -> (String) invokeMethod(m, null, e)))
                .orElse(null);
    }
}