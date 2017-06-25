package org.marid.ide.panes.structure;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;
import org.controlsfx.control.BreadCrumbBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectStructureBreadCrumb extends BreadCrumbBar<Path> {

    @Autowired
    public ProjectStructureBreadCrumb(ProjectStructureTree tree) {
        final ObservableValue<TreeItem<Path>> selectedItem = tree.getSelectionModel().selectedItemProperty();
        final ChangeListener<TreeItem<Path>> treeSelectionChangeListener = (o, oV, nV) -> setSelectedCrumb(nV);
        selectedItem.addListener(treeSelectionChangeListener);
        selectedCrumbProperty().addListener((o, oV, nV) -> {
            selectedItem.removeListener(treeSelectionChangeListener);
            tree.getSelectionModel().select(nV);
            selectedItem.addListener(treeSelectionChangeListener);
            tree.requestFocus();
        });
    }

    @PostConstruct
    public void initCrumbs() {
        final Callback<TreeItem<Path>, Button> crumbFactory = getCrumbFactory();
        setCrumbFactory(param -> {
            final Button button = crumbFactory.call(param);
            button.setText(param.getValue().getFileName().toString());
            return button;
        });
    }
}
