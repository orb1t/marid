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

package org.marid.ide.panes.filebrowser;

import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.Tab;
import org.marid.dependant.beaneditor.BeanEditor;
import org.marid.ide.panes.tabs.IdeTabPane;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10nSupport;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;
import static org.marid.IdeDependants.newNode;
import static org.marid.spring.util.TypedApplicationEventListener.listen;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileEditorLauncher implements L10nSupport {

    private final Provider<BeanFileBrowserTree> beanFileBrowserTree;
    private final Provider<IdeTabPane> ideTabPane;

    @Autowired
    public BeanFileEditorLauncher(Provider<BeanFileBrowserTree> beanFileBrowserTree, Provider<IdeTabPane> ideTabPane) {
        this.beanFileBrowserTree = beanFileBrowserTree;
        this.ideTabPane = ideTabPane;
    }

    public void launch(ActionEvent actionEvent) {
        final BeanFileBrowserTree tree = this.beanFileBrowserTree.get();
        final ProjectProfile profile = tree.getProfile();
        final Path path = tree.getSelectionModel().getSelectedItem().getValue();
        final BeanFile beanFile = requireNonNull(profile.getBeanFiles().get(path));
        final AtomicReference<Tab> tabRef = new AtomicReference<>();
        final MapChangeListener<Path, BeanFile> beanFilesChangeListener = change -> {
            if (change.wasRemoved()) {
                if (path.equals(change.getKey()) && tabRef.get() != null) {
                    ideTabPane.get().getTabs().remove(tabRef.get());
                }
            }
        };
        profile.getBeanFiles().addListener(beanFilesChangeListener);
        final BeanEditor beanEditor = newNode(BeanEditor.class, context -> {
            listen(context, ContextClosedEvent.class, event -> profile.getBeanFiles().removeListener(beanFilesChangeListener));
            final DefaultListableBeanFactory listableBeanFactory = context.getDefaultListableBeanFactory();
            listableBeanFactory.registerSingleton("beanFile", beanFile);
        });
        final Path relativePath = profile.getBeansDirectory().relativize(path);
        final Tab tab = new Tab(s("[%s]: %s", profile, relativePath), beanEditor);
        tabRef.set(tab);
        final IdeTabPane tabPane = ideTabPane.get();
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }
}
