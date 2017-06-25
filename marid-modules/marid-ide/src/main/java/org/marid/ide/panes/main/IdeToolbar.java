package org.marid.ide.panes.main;

import javafx.scene.control.ToolBar;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.spring.annotation.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeToolbar extends ToolBar {

    private final ObjectFactory<Map<String, FxAction>> menuActionsFactory;

    @Autowired
    public IdeToolbar(@IdeAction ObjectFactory<Map<String, FxAction>> menuActionsFactory) {
        this.menuActionsFactory = menuActionsFactory;
    }

    @EventListener
    private void onIdeStart(ContextStartedEvent event) {
        MaridActions.initToolbar(menuActionsFactory.getObject(), this);
    }
}
