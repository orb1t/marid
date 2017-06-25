package org.marid.ide.panes.structure;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectStructurePane extends BorderPane {

    @Autowired
    public ProjectStructurePane(ProjectStructureTree tree, ProjectStructureBreadCrumb crumb) {
        setCenter(tree);
        setBottom(crumb);
        BorderPane.setMargin(crumb, new Insets(2));
    }
}
