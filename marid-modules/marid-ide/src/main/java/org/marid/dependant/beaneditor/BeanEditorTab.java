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

package org.marid.dependant.beaneditor;

import javafx.collections.ListChangeListener;
import javafx.util.Pair;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.tabs.IdeTab;
import org.marid.idefx.controls.IdeShapes;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class BeanEditorTab extends IdeTab {

    public final ProjectProfile profile;
    public final Path beanFilePath;

    @Autowired
    public BeanEditorTab(ProjectProfile profile, BeanListTable table, Path beanFilePath) {
        super(new MaridScrollPane(table), "%s", profile.getBeansDirectory().relativize(beanFilePath));
        setGraphic(IdeShapes.fileNode(profile, beanFilePath, 16));
        this.profile = profile;
        this.beanFilePath = beanFilePath;
    }

    @Override
    public int hashCode() {
        return profile.getName().hashCode() ^ beanFilePath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final BeanEditorTab that = (BeanEditorTab) obj;
        return that.beanFilePath.equals(beanFilePath) && profile.getName().equals(that.profile.getName());
    }

    @Autowired
    private void init(ProjectProfile profile) {
        final ListChangeListener<Pair<Path, BeanFile>> changeListener = c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(e -> {
                        if (e.getKey().equals(beanFilePath)) {
                            getTabPane().getTabs().remove(this);
                        }
                    });
                }
            }
        };
        profile.getBeanFiles().addListener(changeListener);
        setOnCloseRequest(event -> profile.getBeanFiles().removeListener(changeListener));
    }
}
