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

package org.marid.ide.tabs;

import javafx.beans.binding.StringBinding;
import javafx.scene.Node;

import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class IdeTabKey {

    public final StringBinding textBinding;
    public final Supplier<Node> graphicBinding;

    public IdeTabKey(StringBinding textBinding, Supplier<Node> graphicBinding) {
        this.textBinding = textBinding;
        this.graphicBinding = graphicBinding;
    }

    @Override
    public int hashCode() {
        return textBinding.get().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        final IdeTabKey that = (IdeTabKey) obj;
        return this.textBinding.get().equals(that.textBinding.get());
    }
}
