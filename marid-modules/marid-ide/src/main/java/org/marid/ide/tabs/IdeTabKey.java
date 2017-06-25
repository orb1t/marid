/*
 *
 */

package org.marid.ide.tabs;

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

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class IdeTabKey {

    public final ObservableValue<String> textBinding;
    public final Supplier<Node> graphicBinding;

    public IdeTabKey(ObservableValue<String> textBinding, Supplier<Node> graphicBinding) {
        this.textBinding = textBinding;
        this.graphicBinding = graphicBinding;
    }

    @Override
    public int hashCode() {
        return textBinding.getValue().hashCode();
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
        return this.textBinding.getValue().equals(that.textBinding.getValue());
    }
}
