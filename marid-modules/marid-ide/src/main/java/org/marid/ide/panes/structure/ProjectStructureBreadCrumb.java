/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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
