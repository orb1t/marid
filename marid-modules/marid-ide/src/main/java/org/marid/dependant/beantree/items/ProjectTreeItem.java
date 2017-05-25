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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.marid.ide.common.FileActions;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.common.SpecialActionConfiguration;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.LocalizedStrings;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.beans.ConstantValue;
import org.marid.spring.beans.MaridBeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;

import javax.annotation.Nonnull;

import static org.marid.jfx.LocalizedStrings.fs;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@Configurable
public class ProjectTreeItem extends AbstractTreeItem<ProjectProfile> {

    private final ObservableValue<String> name;
    private final ObservableValue<String> type;

    public ProjectTreeItem(ProjectProfile elem) {
        super(elem);
        name = new SimpleStringProperty(elem.getName());
        type = LocalizedStrings.ls("profile");

        valueProperty().bind(Bindings.createObjectBinding(() -> elem, elem.getBeanFiles()));
        graphicProperty().bind(Bindings.createObjectBinding(() -> IdeShapes.profileNode(elem, 20)));
    }

    @Override
    public ObservableValue<String> getName() {
        return name;
    }

    @Override
    public ObservableValue<String> getType() {
        return type;
    }

    @Autowired
    private void initAdd(FileActions fileActions, ProjectProfile profile, FxAction addAction) {
        actionMap.put(SpecialActionConfiguration.ADD, new FxAction("children", "add")
                .setEventHandler(event -> fileActions.addFile(profile))
                .bindText("Add file")
                .setIcon("M_ADD_BOX")
                .setAccelerator(addAction.getAccelerator())
        );
    }

    @Autowired
    private void init(GenericApplicationContext context) {
        destroyActions.add(new ListSynchronizer<>(elem.getBeanFiles(), getChildren(), FileTreeItem::new));
        setExpanded(true);

        ConstantValue.bind(graphic, () -> {
            final HBox box = new HBox(10);
            {
                final Label label = new Label();
                label.setGraphic(glyphIcon("D_FILE", 20));
                label.textProperty().bind(fs("%s: %d", ls("Files"), elem.getBeanFiles().size()));
                box.getChildren().add(label);
            }
            {
                final Label label = new Label();
                label.setGraphic(glyphIcon("D_STAR_CIRCLE", 20));
                final int beanCount = elem.getBeanFiles().stream().mapToInt(f -> f.beans.size()).sum();
                label.textProperty().bind(fs("%s: %d", ls("Beans"), beanCount));
                box.getChildren().add(label);
            }
            {
                final Label label = new Label();
                label.setGraphic(glyphIcon("D_STAR_OUTLINE", 20));
                final long count = elem.getBeanFiles().stream()
                        .flatMap(f -> f.beans.stream())
                        .flatMap(MaridBeanUtils::beans)
                        .count();
                label.textProperty().bind(fs("%s: %d", ls("Inner Beans"), count));
                box.getChildren().add(label);
            }
            return box;
        });
    }

    @Override
    public ResolvableType type() {
        return null;
    }

    @Override
    public int compareTo(@Nonnull AbstractTreeItem<?> o) {
        return 0;
    }
}
