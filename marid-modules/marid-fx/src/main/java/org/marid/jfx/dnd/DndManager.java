/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
