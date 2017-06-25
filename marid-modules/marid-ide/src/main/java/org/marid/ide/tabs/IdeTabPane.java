package org.marid.ide.tabs;

import javafx.scene.control.TabPane;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class IdeTabPane extends TabPane {

    public IdeTabPane() {
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        setFocusTraversable(false);
    }
}
