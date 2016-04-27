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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.TransferMode;

import java.util.function.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class Copies<N, E> {

    private final N node;
    private CopyData<N, E> originalData;
    private final BooleanProperty canTransfer = new SimpleBooleanProperty();

    public Copies(N node) {
        this.node = node;
    }

    public boolean start(E element, TransferMode mode, Function<E, TransferMode[]> modesFunc, Consumer<CopyData<N, E>> task) {
        final TransferMode[] modes = modesFunc.apply(element);
        if (modes.length > 0) {
            originalData = new CopyData<>(mode, node, element, modes);
            canTransfer.set(true);
            task.accept(originalData);
        }
        return modes.length > 0;
    }

    public boolean start(E element, TransferMode mode, Function<E, TransferMode[]> modesFunc) {
        return start(element, mode, modesFunc, d -> {
        });
    }

    public void progress(E element, TransferMode mode, BiFunction<CopyData<N, E>, CopyData<N, E>, TransferMode[]> modesFunc, BiConsumer<CopyData<N, E>, CopyData<N, E>> task) {
        if (mode == null) {
            mode = originalData.transferMode;
        }
        final TransferMode[] transferModes = modesFunc.apply(
                originalData,
                new CopyData<>(mode, originalData.node, element, originalData.transferModes));
        task.accept(originalData, new CopyData<>(mode, originalData.node, element, transferModes));
    }

    public boolean finish(E element, TransferMode mode, BiPredicate<CopyData<N, E>, CopyData<N, E>> task) {
        if (mode == null) {
            mode = originalData.transferMode;
        }
        final CopyData<N, E> original = originalData;
        final CopyData<N, E> target = new CopyData<>(mode, originalData.node, element, originalData.transferModes);
        originalData = null;
        canTransfer.set(false);
        return task.test(original, target);
    }

    public BooleanProperty canTransferProperty() {
        return canTransfer;
    }
}
