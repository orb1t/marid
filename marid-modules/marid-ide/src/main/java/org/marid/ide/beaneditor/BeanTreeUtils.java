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

package org.marid.ide.beaneditor;

import javafx.scene.control.TreeItem;
import javafx.scene.input.TransferMode;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.ConstructorArg;
import org.marid.ide.beaneditor.data.Property;
import org.marid.ide.beaneditor.data.RefValue;
import org.marid.ide.project.ProjectProfile;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTreeUtils {

    private static WeakReference<TreeItem<Object>> sourceRef;

    public static Set<BeanData> beans(TreeItem<Object> item) {
        for (TreeItem<Object> i = item.getParent(); i != null; i = i.getParent()) {
            if (i.getParent() == null) {
                final List<TreeItem<Object>> list = new ArrayList<>();
                items(i, e -> e.getValue() instanceof BeanData, list);
                return list.stream()
                        .map(e -> (BeanData) e.getValue())
                        .collect(toCollection(() -> new TreeSet<>(comparing(e -> e.name.get()))));
            }
        }
        return Collections.emptySet();
    }

    private static void items(TreeItem<Object> item, Predicate<TreeItem<Object>> filter, List<TreeItem<Object>> list) {
        for (final TreeItem<Object> i : item.getChildren()) {
            if (filter.test(i)) {
                list.add(i);
            }
            items(i, filter, list);
        }
    }

    public static boolean isRemovable(TreeItem<Object> treeItem) {
        return treeItem != null
                && !(treeItem.getValue() instanceof ConstructorArg)
                && !(treeItem.getValue() instanceof ProjectProfile)
                && !(treeItem.getValue() instanceof Property);
    }

    public static boolean isRemovable(BeanTree beanTree) {
        return isRemovable(beanTree.getSelectionModel().getSelectedItem());
    }

    public static void remove(TreeItem<Object> treeItem) {
        treeItem.getParent().getChildren().remove(treeItem);
    }

    public static void remove(BeanTree beanTree) {
        remove(beanTree.getSelectionModel().getSelectedItem());
    }

    public static TransferMode[] transferModes(TreeItem<Object> treeItem) {
        if (treeItem.getValue() instanceof BeanData) {
            return TransferMode.ANY;
        } else if (treeItem.getValue() instanceof Path || treeItem.getValue() instanceof Property) {
            return TransferMode.COPY_OR_MOVE;
        } else {
            return TransferMode.NONE;
        }
    }

    public static TransferMode[] transferModes(BeanEditor editor, TreeItem<Object> source, TreeItem<Object> target) {
        if (source.getValue() instanceof BeanData) {
            if (target.getValue() instanceof BeanData) {
                final Path sourcePath = (Path) source.getParent().getValue();
                final Path targetPath = (Path) target.getParent().getValue();
                if (!sourcePath.equals(targetPath)) {
                    return TransferMode.COPY_OR_MOVE;
                }
            } else if (target.getValue() instanceof Path && target.getValue().toString().endsWith(".xml")) {
                final Path sourcePath = (Path) source.getParent().getValue();
                final Path targetPath = (Path) target.getValue();
                if (!sourcePath.equals(targetPath)) {
                    return TransferMode.COPY_OR_MOVE;
                }
            } else if (target.getValue() instanceof RefValue) {
                final ClassData sourceClassData = editor.classData(((BeanData) source.getValue()).type.get());
                final ClassData targetClassData = editor.classData(((RefValue) target.getValue()).type().get());
                if (targetClassData.isAssignableFrom(sourceClassData)) {
                    return new TransferMode[] {TransferMode.LINK};
                }
            }
        } else if (source.getValue() instanceof Path) {
            if (target.getValue() instanceof Path || target.getValue() instanceof ProjectProfile) {
                return TransferMode.COPY_OR_MOVE;
            }
        }
        return TransferMode.NONE;
    }

    public static void copy(TransferMode transferMode, TreeItem<Object> source, TreeItem<Object> target) {
        switch (transferMode) {
            case LINK:
                if (target.getValue() instanceof RefValue && source.getValue() instanceof BeanData) {
                    final RefValue refValue = (RefValue) target.getValue();
                    final BeanData beanData = (BeanData) source.getValue();
                    refValue.ref().bind(beanData.name);
                }
                break;
            case MOVE:
                if (source.getValue() instanceof BeanData) {
                    if (target.getValue() instanceof Path) {
                        remove(source);
                        target.getChildren().add(source);
                    }
                }
                if (source.getValue() instanceof Path) {
                    if (target.getValue() instanceof Path || target.getValue() instanceof ProjectProfile) {
                        remove(source);
                        target.getChildren().add(source);
                    }
                }
                break;
        }
    }

    public static void startCopy(TreeItem<Object> item, BiConsumer<TreeItem<Object>, TransferMode[]> task) {
        final TransferMode[] transferModes = transferModes(item);
        if (transferModes.length > 0) {
            sourceRef = new WeakReference<>(item);
            task.accept(item, transferModes);
        }
    }

    public static void progressCopy(BeanEditor editor, TreeItem<Object> target, BiConsumer<TreeItem<Object>, TransferMode[]> task) {
        final TransferMode[] transferModes = transferModes(editor, sourceRef.get(), target);
        task.accept(target, transferModes);
    }

    public static boolean finishCopy(TransferMode transferMode, TreeItem<Object> target) {
        try {
            copy(transferMode, sourceRef.get(), target);
            return true;
        } catch (Exception x) {
            return false;
        }
    }
}
