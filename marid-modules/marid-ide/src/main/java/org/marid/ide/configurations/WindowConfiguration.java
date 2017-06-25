package org.marid.ide.configurations;

import javafx.scene.input.KeyCodeCombination;
import org.marid.Ide;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;

import static javafx.scene.input.KeyCode.F12;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class WindowConfiguration {

    @IdeAction
    public FxAction alwaysOnTopAction() {
        return new FxAction("ops", "Window")
                .bindText("Always on top")
                .setIcon("D_BORDER_TOP")
                .setAccelerator(new KeyCodeCombination(F12, CONTROL_DOWN))
                .bindSelected(Ide.primaryStage.alwaysOnTopProperty())
                .setEventHandler(event -> Ide.primaryStage.setAlwaysOnTop(!Ide.primaryStage.isAlwaysOnTop()));
    }

    @IdeAction
    public FxAction fullScreenAction() {
        return new FxAction("ops", "Window")
                .bindText("Fullscreen")
                .setIcon("D_FULLSCREEN")
                .setAccelerator(new KeyCodeCombination(F12))
                .bindSelected(Ide.primaryStage.fullScreenProperty())
                .setEventHandler(event -> Ide.primaryStage.setFullScreen(!Ide.primaryStage.isFullScreen()));
    }
}
