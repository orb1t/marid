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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.ConstructorArg;
import org.marid.ide.beaneditor.data.Property;
import org.marid.ide.beaneditor.data.RefValue;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.copy.CopyData;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static org.marid.l10n.L10nSupport.LS.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTreeUtils implements BeanTreeConstants {

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

    public static TransferMode[] transferModes(CopyData<BeanEditor, TreeItem<Object>> sourceData, CopyData<BeanEditor, TreeItem<Object>> targetData) {
        final TreeItem<Object> source = sourceData.element;
        final TreeItem<Object> target = targetData.element;
        final BeanEditor editor = sourceData.node;
        if (source.getValue() instanceof BeanData) {
            if (target.getValue() instanceof BeanData) {
                if (targetData.transferMode == TransferMode.COPY) {
                    return new TransferMode[]{TransferMode.COPY};
                }
                final Path sourcePath = (Path) source.getParent().getValue();
                final Path targetPath = (Path) target.getParent().getValue();
                if (!sourcePath.equals(targetPath)) {
                    return TransferMode.COPY_OR_MOVE;
                }
            } else if (target.getValue() instanceof Path && target.getValue().toString().endsWith(".xml")) {
                if (targetData.transferMode == TransferMode.COPY) {
                    return new TransferMode[]{TransferMode.COPY};
                }
                final Path sourcePath = (Path) source.getParent().getValue();
                final Path targetPath = (Path) target.getValue();
                if (!sourcePath.equals(targetPath)) {
                    return TransferMode.COPY_OR_MOVE;
                }
            } else if (target.getValue() instanceof RefValue) {
                final ClassData sourceClassData = editor.classData(((BeanData) source.getValue()).type.get());
                final ClassData targetClassData = editor.classData(((RefValue) target.getValue()).type().get());
                if (targetClassData.isAssignableFrom(sourceClassData)) {
                    return new TransferMode[]{TransferMode.LINK};
                }
            }
        } else if (source.getValue() instanceof Path) {
            if (target.getValue() instanceof Path && !target.getValue().toString().endsWith(".xml")
                    || target.getValue() instanceof ProjectProfile) {
                return TransferMode.COPY_OR_MOVE;
            }
        }
        return TransferMode.NONE;
    }

    public static void copy(BeanEditor editor, TransferMode transferMode, TreeItem<Object> source, TreeItem<Object> target) {
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
                    remove(source);
                    if (target.getValue() instanceof Path) {
                        target.getChildren().add(source);
                    } else {
                        final int index = target.getParent().getChildren().indexOf(target);
                        target.getParent().getChildren().add(index, source);
                    }
                } else if (source.getValue() instanceof Path) {
                    if (target.getValue() instanceof Path || target.getValue() instanceof ProjectProfile) {
                        remove(source);
                        target.getChildren().add(source);
                    }
                }
                break;
            case COPY:
                if (source.getValue() instanceof BeanData) {
                    final TreeItem<Object> item = copy(editor, (BeanData) source.getValue(), source);
                    if (target.getValue() instanceof Path) {
                        target.getChildren().add(item);
                    } else {
                        final int index = target.getParent().getChildren().indexOf(target);
                        target.getParent().getChildren().add(index, item);
                    }
                } else if (source.getValue() instanceof Path) {
                    target.getChildren().add(copy(editor, source, target));
                }
                break;
        }
    }

    private static TreeItem<Object> copy(BeanEditor editor, TreeItem<Object> source, TreeItem<Object> target) {
        final boolean sourceXml = source.getValue().toString().endsWith(".xml");
        final Set<String> names = target.getChildren().stream()
                .map(e -> ((Path) e.getValue()).getFileName().toString())
                .collect(toSet());
        final Path parent = target.getValue() instanceof ProjectProfile
                ? ((ProjectProfile) target.getValue()).getBeansDirectory()
                : ((Path) target.getValue());
        final String name;
        {
            final StringBuilder builder = new StringBuilder(((Path) source.getValue()).getFileName().toString());
            if (sourceXml) {
                builder.delete(builder.length() - 4, builder.length());
            }
            while (names.contains(sourceXml ? builder + ".xml" : builder.toString())) {
                builder.append('_').append(s("copy"));
            }
            if (sourceXml) {
                builder.append(".xml");
            }
            name = builder.toString();
        }
        final Path path = parent.resolve(name);
        final TreeItem<Object> item = new TreeItem<>(path, new ImageView(sourceXml ? FILE : DIR));
        for (final TreeItem<Object> child : source.getChildren()) {
            if (child.getValue() instanceof Path) {
                item.getChildren().add(copy(editor, child, item));
            } else if (child.getValue() instanceof BeanData) {
                item.getChildren().add(copy(editor, (BeanData) child.getValue(), child));
            }
        }
        return item;
    }

    private static TreeItem<Object> copy(BeanEditor editor, BeanData sourceBeanData, TreeItem<Object> source) {
        final Map<String, StringProperty> nameMap = beans(source).stream().collect(toMap(b -> b.name.get(), b -> b.name));
        final BiConsumer<StringProperty, StringProperty> nameConsumer = (src, tgt) -> {
            final StringProperty bound = nameMap.get(src.get());
            if (bound == null) {
                tgt.set(src.get());
            } else {
                tgt.bind(bound);
            }
        };
        final String sourceName = sourceBeanData.name.get();
        final String targetName;
        {
            final String copyString = s("copy");
            final StringBuilder builder = new StringBuilder(sourceName).append('_').append(copyString);
            while (nameMap.containsKey(builder.toString())) {
                builder.append('_').append(copyString);
            }
            targetName = builder.toString();
        }
        final BeanData td = new BeanData();
        td.name.set(targetName);
        td.type.set(sourceBeanData.type.get());
        td.destroyMethod.set(sourceBeanData.destroyMethod.get());
        td.initMethod.set(sourceBeanData.initMethod.get());
        td.lazyInit.set(sourceBeanData.lazyInit.get());
        td.factoryMethod.set(sourceBeanData.factoryMethod.get());
        nameConsumer.accept(sourceBeanData.factoryBean, td.factoryBean);
        final TreeItem<Object> targetItem = new TreeItem<>(td, new ImageView(editor.image(td.type.get())));
        for (final TreeItem<Object> child : source.getChildren()) {
            if (child.getValue() instanceof ConstructorArg) {
                final ConstructorArg s = (ConstructorArg) child.getValue();
                final ConstructorArg d = new ConstructorArg();
                d.type.set(s.type.get());
                d.name.set(s.name.get());
                if (s.value.isNotEmpty().get()) {
                    d.value.set(s.value.get());
                } else if (s.ref.isBound()) {
                    nameConsumer.accept(s.ref, d.ref);
                }
                targetItem.getChildren().add(new TreeItem<>(d, new ImageView(editor.image(d.type.get()))));
            } else if (child.getValue() instanceof Property) {
                final Property s = (Property) child.getValue();
                final Property d = new Property();
                d.type.set(s.type.get());
                d.name.set(s.name.get());
                if (s.value.isNotEmpty().get()) {
                    d.value.set(s.value.get());
                } else if (s.ref.isBound()) {
                    nameConsumer.accept(s.ref, d.ref);
                }
                targetItem.getChildren().add(new TreeItem<>(d, new ImageView(editor.image(d.type.get()))));
            }
        }
        return targetItem;
    }

    public static boolean finishCopy(CopyData<BeanEditor, TreeItem<Object>> sourceData, CopyData<BeanEditor, TreeItem<Object>> targetData) {
        final List<TransferMode> transferModes = Arrays.asList(transferModes(sourceData, targetData));
        if (transferModes.isEmpty()) {
            return false;
        }
        final TransferMode mode = transferModes.contains(targetData.transferMode)
                ? targetData.transferMode
                : transferModes.iterator().next();
        try {
            copy(sourceData.node, mode, sourceData.element, targetData.element);
            return true;
        } catch (Exception x) {
            return false;
        }
    }

    public static boolean cutOrCopyDisabled(TreeItem<Object> item) {
        if (item == null) {
            return true;
        }
        if (item.getValue() instanceof BeanData) {
            return false;
        }
        if (item.getValue() instanceof Path) {
            return false;
        }
        return true;
    }

    public static BooleanBinding cutOrCopyDisabled(BeanEditor editor) {
        return Bindings.createBooleanBinding(
                () -> cutOrCopyDisabled(editor.beanTree.getSelectionModel().getSelectedItem()),
                editor.beanTree.getSelectionModel().selectedItemProperty());
    }

    public static boolean pasteDisabled(BeanEditor editor, TreeItem<Object> item) {
        if (!editor.copies.canTransferProperty().get()) {
            return true;
        }
        final AtomicBoolean disabled = new AtomicBoolean(true);
        editor.copies.progress(item, null, BeanTreeUtils::transferModes, (s, t) -> {
            if (t.transferModes.length > 0) {
                disabled.set(false);
            }
        });
        return disabled.get();
    }

    public static BooleanBinding pasteDisabled(BeanEditor editor) {
        return Bindings.createBooleanBinding(
                () -> pasteDisabled(editor, editor.beanTree.getSelectionModel().getSelectedItem()),
                editor.beanTree.getSelectionModel().selectedItemProperty(), editor.copies.canTransferProperty());
    }
}
