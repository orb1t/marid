/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.jfx.dnd;

import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;

import java.lang.ref.WeakReference;

public class DndManager {

    private WeakReference<Dragboard> dragboardRef;

    public Clipboard clipboard() {
        final WeakReference<Dragboard> ref = dragboardRef;
        if (ref == null) {
            return Clipboard.getSystemClipboard();
        }
        final Dragboard dragboard = ref.get();
        if (dragboard == null) {
            return Clipboard.getSystemClipboard();
        }
        return dragboard;
    }

    public void updateDragboard(Dragboard dragboard) {
        dragboardRef = new WeakReference<>(dragboard);
    }
}
