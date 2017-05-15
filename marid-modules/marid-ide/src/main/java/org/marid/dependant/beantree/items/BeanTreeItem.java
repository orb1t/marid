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
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.icons.FontIcons;
import org.marid.spring.xml.BeanData;
import org.marid.util.MethodUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Comparator;

import static org.marid.ide.common.SpecialActionConfiguration.RENAME;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Configurable
public class BeanTreeItem extends AbstractTreeItem<BeanData> {

    public BeanTreeItem(BeanData elem) {
        super(elem);
        valueProperty().bind(Bindings.createObjectBinding(() -> elem, elem));
        graphicProperty().bind(Bindings.createObjectBinding(() -> IdeShapes.beanNode(elem, 20), elem));
    }

    @Override
    public ObservableValue<String> getName() {
        return elem.name;
    }

    @Override
    public ObservableValue<String> getType() {
        final ProjectProfile profile = getProfile();
        return Bindings.createStringBinding(() -> MethodUtils.readableType(profile.getType(elem)), elem);
    }

    @Override
    public ObservableValue<String> valueText() {
        return Bindings.createObjectBinding(() -> {
            if (elem.isFactoryBean()) {
                if (elem.getFactoryBean() != null) {
                    return String.format("%s.%s", elem.getFactoryBean(), elem.getFactoryMethod());
                } else {
                    return String.format("%s.%s", elem.getType(), elem.getFactoryMethod());
                }
            }
            return null;
        }, elem);
    }

    @Override
    public ObservableValue<Node> valueGraphic() {
        return Bindings.createObjectBinding(() -> {
            final HBox box = new HBox(10);
            if (elem.isFactoryBean()) {
                final Node icon = FontIcons.glyphIcon("D_LINK", 20);
                box.getChildren().add(icon);
            }
            return box;
        }, elem);
    }

    @Autowired
    private void init(GenericApplicationContext context) {
        destroyActions.add(0, new ListSynchronizer<>(elem.beanArgs, getChildren(), ArgumentTreeItem::new));
        destroyActions.add(0, new ListSynchronizer<>(elem.properties, getChildren(), PropertyTreeItem::new));
        setExpanded(true);
    }

    @Autowired
    private void initUpdateAction(ProjectProfile profile) {

    }

    @Autowired
    private void initRenameAction(ProjectProfile profile) {
        actionMap.put(RENAME, new FxAction("a", "Actions")
                .bindText("Rename")
                .setEventHandler(event -> {
                    final TextInputDialog nameDialog = new TextInputDialog(elem.getName());
                    nameDialog.getDialogPane().setPrefWidth(800);
                    nameDialog.titleProperty().bind(ls("Rename bean"));
                    nameDialog.showAndWait().ifPresent(value -> {
                        if (value.equals(elem.getName())) {
                            return;
                        }
                        final String oldName = elem.getName();
                        final String newName = profile.generateBeanName(value);
                        elem.setName(newName);
                        ProjectManager.onBeanNameChange(profile, oldName, newName);
                    });
                })
        );
    }

    @Override
    public int compareTo(@NotNull AbstractTreeItem<?> o) {
        if (o instanceof BeanTreeItem) {
            final BeanTreeItem that = (BeanTreeItem) o;
            final Comparator<String> c = Comparator.nullsFirst(String::compareTo);
            return c.compare(this.elem.getName(), that.elem.getName());
        } else {
            return 0;
        }
    }
}
