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

package org.marid.ee.ui;

import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.jboss.weld.context.AbstractSharedContext;
import org.marid.ide.Ide;
import org.marid.logging.LogSupport;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import java.lang.annotation.Annotation;

/**
 * @author Dmitry Ovchinnikov
 */
public class UIContext extends AbstractSharedContext implements LogSupport {

    public UIContext() {
        super(Ide.class.getName());
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return UI.class;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        super.destroy(contextual);
        log(FINE, "{0} destroyed", contextual);
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        final T instance = super.get(contextual, creationalContext);
        if (instance instanceof Node) {
            final Node node = (Node) instance;
            node.sceneProperty().addListener((observableNode, oldScene, newScene) -> {
                if (newScene == null) {
                    return;
                }
                if (newScene.getWindow() == null) {
                    newScene.windowProperty().addListener((observableScene, oldWindow, newWindow) -> {
                        if (newWindow == null) {
                            return;
                        }
                        newWindow.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> destroy(contextual));
                    });
                } else {
                    final Window window = newScene.getWindow();
                    window.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> destroy(contextual));
                }
            });
        } else if (instance instanceof Window) {
            final Window window = (Window) instance;
            window.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> destroy(contextual));
        } else if (instance instanceof Dialog) {
            final Dialog<?> dialog = (Dialog) instance;
            dialog.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    destroy(contextual);
                }
            });
        }
        return instance;
    }
}
