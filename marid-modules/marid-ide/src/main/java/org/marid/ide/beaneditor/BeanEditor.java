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

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.marid.beans.MaridBeanXml;
import org.marid.beans.MaridBeansXml;
import org.marid.ee.IdeSingleton;
import org.marid.ide.Ide;
import org.marid.ide.beaneditor.data.Loader;
import org.marid.ide.beaneditor.data.Saver;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.copy.Copies;
import org.marid.jfx.menu.MenuContainerBuilder;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static javafx.scene.input.TransferMode.COPY;
import static javafx.scene.input.TransferMode.MOVE;
import static org.marid.ide.beaneditor.BeanTreeUtils.isRemovable;
import static org.marid.xml.XmlBind.load;

/**
 * @author Dmitry Ovchinnikov
 */
@IdeSingleton
public class BeanEditor extends Stage implements LogSupport, L10nSupport, PrefSupport {

    private URLClassLoader classLoader;
    final ProjectManager projectManager;
    final BeanTree beanTree;
    final List<MaridBeanXml> metaBeans = new ArrayList<>();
    final Map<String, String> typeUrlMap = new HashMap<>();
    final Map<String, Image> iconMap = new HashMap<>();
    final Map<String, ClassData> classDataMap = new HashMap<>();
    final Copies<BeanEditor, TreeItem<Object>> copies;
    final Loader loader;
    final Saver saver;

    @Inject
    public BeanEditor(ProjectManager projectManager) {
        final ProjectProfile profile = projectManager.getProfile();
        this.projectManager = projectManager;
        getIcons().addAll(Ide.IMAGES);
        projectManager.profileProperty().addListener((observable, oldValue, newValue) -> update(newValue));
        this.loader = new Loader(this);
        this.saver = new Saver();
        this.beanTree = new BeanTree(profile, this);
        this.copies = new Copies<>(this);
        final BorderPane pane = getTreePane();
        setScene(new Scene(pane, 1500, 800));
        update(profile);
    }

    public Copies<BeanEditor, TreeItem<Object>> getCopies() {
        return copies;
    }

    public Loader getLoader() {
        return loader;
    }

    private BorderPane getTreePane() {
        final ToolBar toolBar = new ToolBar();
        final MenuBar menuBar = new MenuBar();
        new MenuContainerBuilder()
                .menu("File", true, b -> b
                        .item("Reload", mb -> mb
                                .accelerator("Ctrl+R")
                                .icon(MaterialIcon.RESTORE)
                                .action(event -> {
                                    beanTree.getRoot().getChildren().clear();
                                    beanTree.load();
                                })
                        )
                        .item("Save", mb -> mb
                                .accelerator("Ctrl+S")
                                .icon(MaterialIcon.SAVE)
                                .action(event -> beanTree.save())
                        )
                        .separator()
                        .item("Print", mb -> mb
                                .accelerator("Ctrl+P")
                                .icon(MaterialIcon.PRINT)
                                .action(event -> {
                                })
                        )
                )
                .menu("Edit", true, b -> b
                        .item("Cut", mb -> mb
                                .accelerator("Ctrl+X")
                                .icon(MaterialDesignIcon.CONTENT_CUT)
                                .action(event -> copies.start(currentItem(), MOVE, BeanTreeUtils::transferModes))
                                .disabled(BeanTreeUtils.cutOrCopyDisabled(this))
                        )
                        .item("Copy", mb -> mb
                                .accelerator("Ctrl+C")
                                .icon(MaterialDesignIcon.CONTENT_COPY)
                                .action(event -> copies.start(currentItem(), COPY, BeanTreeUtils::transferModes))
                                .disabled(BeanTreeUtils.cutOrCopyDisabled(this))
                        )
                        .item("Paste", mb -> mb
                                .accelerator("Ctrl+V")
                                .icon(MaterialDesignIcon.CONTENT_PASTE)
                                .action(event -> copies.finish(currentItem(), null, BeanTreeUtils::finishCopy))
                                .disabled(BeanTreeUtils.pasteDisabled(this))
                        )
                )
                .menu("Beans", true, b -> b
                        .item("Remove", mb -> mb
                                .accelerator("F8")
                                .icon(MaterialIcon.REMOVE)
                                .action(event -> BeanTreeUtils.remove(beanTree))
                                .disabled(Bindings.createBooleanBinding(
                                        () -> !isRemovable(beanTree),
                                        beanTree.getSelectionModel().selectedItemProperty()))
                        )
                        .separator()
                        .item("Clear all", mb -> mb
                                .accelerator("F9")
                                .icon(MaterialIcon.CLEAR_ALL)
                                .action(event -> beanTree.getRoot().getChildren().clear())
                                .disabled(beanTree.clearAllDisabled())
                        )
                )
                .menu("Window", true, b -> b
                        .item("Refresh", mb -> mb
                                .accelerator("F5")
                                .icon(MaterialDesignIcon.REFRESH)
                                .action(event -> beanTree.refresh())
                        )
                )
                .build(menuBar.getMenus()::add, toolBar.getItems()::add);
        final VBox vBox = new VBox(menuBar, toolBar);
        final BorderPane pane = new BorderPane(ScrollPanes.scrollPane(beanTree), vBox, null, null, null);
        pane.setFocusTraversable(false);
        return pane;
    }

    private TreeItem<Object> currentItem() {
        return beanTree.getSelectionModel().getSelectedItem();
    }

    void update(ProjectProfile profile) {
        closeClassLoader();
        classLoader = profile.classLoader();
        classDataMap.clear();
        reloadBeanXmls();
        setTitle(s("Bean editor: [%s]", profile));
        beanTree.update(profile);
    }

    void closeClassLoader() {
        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (Exception x) {
                log(WARNING, "Unable to close the associated classloader", x);
            }
        }
    }

    void reloadBeanXmls() {
        metaBeans.clear();
        typeUrlMap.clear();
        iconMap.clear();
        try {
            for (final Enumeration<URL> e = classLoader.findResources("maridBeans.xml"); e.hasMoreElements(); ) {
                final URL url = e.nextElement();
                try (final InputStream inputStream = url.openStream()) {
                    final Source inputSource = new StreamSource(inputStream);
                    final MaridBeansXml beans = load(MaridBeansXml.class, inputSource, Unmarshaller::unmarshal);
                    for (final MaridBeanXml beanXml : beans.beans) {
                        metaBeans.add(beanXml);
                        if (beanXml.icon != null) {
                            typeUrlMap.put(beanXml.type, beanXml.icon);
                        }
                    }
                } catch (Exception x) {
                    log(WARNING, "Unable to process {0}", x, url);
                }
            }
        } catch (Exception x) {
            log(WARNING, "Unable to enumerate marid beans", x);
        }
        try {
            for (final Enumeration<URL> e = classLoader.findResources("typeicons.properties"); e.hasMoreElements(); ) {
                final URL url = e.nextElement();
                try (final InputStream inputStream = url.openStream()) {
                    final Properties properties = new Properties();
                    properties.load(inputStream);
                    for (final String key : properties.stringPropertyNames()) {
                        final String[] types = key.split(",");
                        for (final String type : types) {
                            typeUrlMap.put(type, properties.getProperty(key));
                        }
                    }
                } catch (Exception x) {
                    log(WARNING, "Unable to process {0}", x, url);
                }
            }
        } catch (Exception x) {
            log(WARNING, "Unable to enumerate typeicons", x);
        }
    }

    public Image image(@Nonnull String type) {
        return iconMap.compute(type, (k, img) -> {
            if (img != null) {
                return img;
            } else {
                if (k == null) {
                    return null;
                }
                try {
                    final String url = typeUrlMap.get(k);
                    return url == null ? null : new Image(url, 24, 24, false, true, true);
                } catch (Exception x) {
                    log(WARNING, "Image loading error for type {0}", x, k);
                    return null;
                }
            }
        });
    }

    public ClassData classData(String type) {
        return classDataMap.computeIfAbsent(type, t -> {
            switch (t) {
                case "boolean":
                    return new ClassData(Boolean.class);
                case "byte":
                    return new ClassData(Byte.class);
                case "char":
                    return new ClassData(Character.class);
                case "int":
                    return new ClassData(Integer.class);
                case "long":
                    return new ClassData(Long.class);
                case "double":
                    return new ClassData(Double.class);
                case "float":
                    return new ClassData(Float.class);
                case "void":
                    return new ClassData(Void.class);
                case "short":
                    return new ClassData(Short.class);
                default:
                    try {
                        final Class<?> klass = Class.forName(t, false, classLoader);
                        return new ClassData(klass);
                    } catch (Exception x) {
                        log(WARNING, "Unable to load {0}", x, type);
                        return new ClassData(Object.class);
                    }
            }
        });
    }

    public GlyphIcons accessorIcon(int modifiers) {
        if (Modifier.isPublic(modifiers)) {
            return OctIcon.MIRROR_PUBLIC;
        } else if (Modifier.isProtected(modifiers)) {
            return MaterialDesignIcon.NEST_PROTECT;
        } else if (Modifier.isPrivate(modifiers)) {
            return OctIcon.MIRROR_PRIVATE;
        } else {
            return OctIcon.PACKAGE;
        }
    }

    public List<MaridBeanXml> getMetaBeans() {
        return metaBeans;
    }
}
