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

package org.marid.dependant.beaneditor.view;

import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.marid.expression.mutable.Expr;

import javax.annotation.Nonnull;

public abstract class IdeBeanView extends HBox {

	public static IdeBeanView view(@Nonnull Expr expr) {
		return null;
	}

	public static Text glyph(@Nonnull Expr expr) {
		return null;
	}
}