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

package org.marid.dependant.beantree.items;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.marid.ide.common.FileActions;
import org.marid.ide.common.SpecialActionConfiguration;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.spring.beans.MaridBeanUtils;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Nonnull;
import java.util.Comparator;

import static org.marid.ide.common.IdeShapes.fileNode;
import static org.marid.jfx.LocalizedStrings.fs;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@Configurable
public class FileTreeItem extends AbstractTreeItem<BeanFile> {

    private final ObservableValue<String> name;
    private final ObservableValue<String> type;

    public FileTreeItem(BeanFile file) {
        super(file);

        name = Bindings.createStringBinding(file::getFilePath, file.path);
        type = ls("file");

        graphicProperty().bind(Bindings.createObjectBinding(() -> fileNode(file, 20), file));
    }

    @Override
    public ObservableValue<String> getName() {
        return name;
    }

    @Override
    public ObservableValue<String> getType() {
        return type;
    }

    @Override
    public Node graphic() {
        final HBox box = new HBox(10);
        {
            final Label label = new Label();
            label.setGraphic(glyphIcon("D_STAR_CIRCLE", 20));
            label.textProperty().bind(fs("%s: %d", ls("Beans"), elem.beans.size()));
            box.getChildren().add(label);
        }
        {
            final Label label = new Label();
            label.setGraphic(glyphIcon("D_STAR_OUTLINE", 20));
            final long count = elem.beans.stream().flatMap(MaridBeanUtils::beans).count();
            label.textProperty().bind(fs("%s: %d", ls("Inner Beans"), count));
            box.getChildren().add(label);
        }
        return box;
    }

    @Override
    public String text() {
        return null;
    }

    @Autowired
    private void initRename(ProjectProfile profile, FileActions actions, FxAction renameAction) {
        actionMap.put(SpecialActionConfiguration.RENAME, new FxAction("op", "rename")
                .setEventHandler(event -> actions.renameFile(profile, elem))
                .setAccelerator(renameAction.getAccelerator())
                .setIcon("O_DIFF_RENAMED")
                .bindText("Rename file")
        );
    }

    @Autowired
    private void initRemove(ProjectProfile profile, FxAction removeAction) {
        actionMap.put(SpecialActionConfiguration.REMOVE, new FxAction("op", "remove")
                .setEventHandler(event -> profile.getBeanFiles().remove(elem))
                .setAccelerator(removeAction.getAccelerator())
                .setIcon("D_TABLE_ROW_REMOVE")
                .bindText("Remove file")
        );
    }

    @Autowired
    private void init(GenericApplicationContext context) {
        destroyActions.add(0, new ListSynchronizer<>(elem.beans, getChildren(), BeanTreeItem::new));
        setExpanded(true);
    }

    @Override
    public int compareTo(@Nonnull AbstractTreeItem<?> o) {
        if (o instanceof FileTreeItem) {
            final FileTreeItem that = (FileTreeItem) o;
            final Comparator<String> c = Comparator.nullsFirst(String::compareTo);
            return c.compare(this.elem.getFilePath(), that.elem.getFilePath());
        } else {
            return 0;
        }
    }
}
