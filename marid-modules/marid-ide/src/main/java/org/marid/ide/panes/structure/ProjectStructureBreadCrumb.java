/*
 *
 */

package org.marid.ide.panes.structure;

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
