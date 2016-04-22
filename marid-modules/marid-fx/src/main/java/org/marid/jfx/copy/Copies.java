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

package org.marid.jfx.copy;

import javafx.scene.input.TransferMode;

import java.util.function.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Copies<N, E> {

    private final N node;
    private CopyData<N, E> originalData;

    public Copies(N node) {
        this.node = node;
    }

    public void start(E element, Function<E, TransferMode[]> modesFunc, Consumer<CopyData<N, E>> task) {
        final TransferMode[] modes = modesFunc.apply(element);
        originalData = new CopyData<>(null, node, element, modes);
        if (modes.length > 0) {
            task.accept(originalData);
        }
    }

    public void progress(E element,
                         TransferMode transferMode,
                         BiFunction<CopyData<N, E>, CopyData<N, E>, TransferMode[]> modesFunc,
                         BiConsumer<CopyData<N, E>, CopyData<N, E>> task) {
        final TransferMode[] transferModes = modesFunc.apply(
                originalData,
                new CopyData<>(transferMode, originalData.node, element, originalData.transferModes));
        task.accept(originalData, new CopyData<>(transferMode, originalData.node, element, transferModes));
    }

    public boolean finish(E element,
                          TransferMode transferMode,
                          BiPredicate<CopyData<N, E>, CopyData<N, E>> task) {
        final CopyData<N, E> original = originalData;
        final CopyData<N, E> target = new CopyData<>(transferMode, originalData.node, element, originalData.transferModes);
        originalData = null;
        System.out.printf("%s, %s%n", original.element, target.element);
        return task.test(original, target);
    }
}
