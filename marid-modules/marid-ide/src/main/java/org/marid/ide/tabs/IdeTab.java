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

package org.marid.ide.tabs;

import javafx.scene.control.Tab;
import org.marid.jfx.props.Props;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PreDestroy;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov.
 */
public class IdeTab extends Tab {

  @Autowired
  private void init(GenericApplicationContext context, IdeTabPane tabPane) {
    tabPane.getTabs().add(this);
    tabPane.getSelectionModel().select(this);
    Props.addHandler(onClosedProperty(), event -> context.close());
  }

  @PreDestroy
  private void destroy() {
    Optional.ofNullable(getTabPane()).ifPresent(tabPane -> tabPane.getTabs().remove(this));
  }
}
