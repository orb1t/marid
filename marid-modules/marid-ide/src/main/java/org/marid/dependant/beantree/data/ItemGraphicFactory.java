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
import javafx.scene.Node;
import org.marid.ide.common.IdeShapes;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.DElement;
import org.marid.spring.xml.DRef;

import java.util.Optional;

import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ItemGraphicFactory {

    static Node graphic(DRef ref) {
        return IdeShapes.ref(ref, 20);
    }

    static Node graphic(BeanData beanData) {
        return IdeShapes.beanNode(beanData, 20);
    }

    static Node graphic(ObservableValue<DElement> element) {
        return Optional.ofNullable(element.getValue())
                .flatMap(e -> Optional.ofNullable(findMethod(ItemGraphicFactory.class, "graphic", e.getClass()))
                        .map(m -> (Node) invokeMethod(m, null, e)))
                .orElse(null);
    }
}
