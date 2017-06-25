package org.marid.dependant.beaneditor;

import javafx.beans.value.ObservableStringValue;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import org.marid.ide.model.TextFile;
import org.marid.ide.tabs.IdeTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanEditorTab extends IdeTab {

    @Autowired
    public BeanEditorTab(SplitPane beanSplitPane,
                         ObservableStringValue beanEditorTabText,
                         Supplier<Node> beanEditorGraphic,
                         TextFile javaFile) {
        super(beanSplitPane, beanEditorTabText, beanEditorGraphic);
        addNodeObservables(javaFile.path);
    }
}
