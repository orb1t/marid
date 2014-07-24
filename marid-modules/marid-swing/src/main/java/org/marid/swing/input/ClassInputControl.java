/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.swing.input;

import org.marid.logging.LogSupport;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import static java.lang.Thread.currentThread;

/**
 * @author Dmitry Ovchinnikov
 */
public class ClassInputControl extends JPanel implements InputControl<Class<?>>, LogSupport {

    protected final JTree tree;
    protected final JTextField classField = new JTextField();
    protected final ClassTreeModel model;

    public ClassInputControl(ClassLoader classLoader) {
        super(new BorderLayout());
        model = new ClassTreeModel(classNames(classLoader));
        add(new JScrollPane(tree = new JTree(model)));
        add(classField, BorderLayout.NORTH);
        tree.setShowsRootHandles(false);
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(e -> {
            if (e.getPath().getPathCount() == 3) {
                final StringJoiner joiner = new StringJoiner(".");
                for (int i = 1; i < 3; i++) {
                    joiner.add(e.getPath().getPathComponent(i).toString());
                }
                classField.setText(joiner.toString());
            }
        });
    }

    public ClassInputControl() {
        this(currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> getInputValue() {
        final String className = classField.getText().trim();
        try {
            return className.isEmpty() ? null : Class.forName(className, false, currentThread().getContextClassLoader());
        } catch (Exception x) {
            warning("Unable to load class {0}", x, className);
            return null;
        }
    }

    @Override
    public void setInputValue(Class<?> value) {
        if (value == null) {
            return;
        }
        classField.setText(value.getName());
        if (value.getPackage() == null) {
            return;
        }
        final String[] parts = value.getName().split("[.]");
        tree.setSelectionPath(new TreePath(new String[]{"", value.getPackage().getName(), parts[parts.length - 1]}));
        tree.expandPath(new TreePath(new String[]{"", value.getPackage().getName()}));
        tree.scrollPathToVisible(tree.getSelectionPath());
    }

    private Map<String, Collection<String>> classNames(ClassLoader classLoader) {
        final Map<String, Collection<String>> map = new ConcurrentHashMap<>();
        processClasses(classLoader, classLoader, map);
        return map;
    }

    private void processClasses(ClassLoader classLoader, ClassLoader mainLoader, Map<String, Collection<String>> map) {
        if (classLoader == null) {
            return;
        }
        if (classLoader instanceof URLClassLoader) {
            final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            final URL[] urls = urlClassLoader.getURLs();
            if (urls != null) {
                final List<ForkJoinTask<?>> tasks = new ArrayList<>(urls.length);
                for (final URL url : urls) {
                    tasks.add(ForkJoinPool.commonPool().submit(() -> {
                        final Path path = Paths.get(url.toURI());
                        if (Files.isDirectory(path)) {
                            processDirectory(path, map);
                        } else if (path.getFileName().toString().endsWith(".jar")) {
                            try (final FileSystem fs = FileSystems.newFileSystem(path, mainLoader)) {
                                for (final Path root : fs.getRootDirectories()) {
                                    processDirectory(root, map);
                                }
                            }
                        }
                        return null;
                    }));
                }
                for (final ForkJoinTask<?> task : tasks) {
                    try {
                        task.join();
                    } catch (Exception x) {
                        warning("Unable to process task", x);
                    }
                }
            }
        }
        processClasses(classLoader.getParent(), mainLoader, map);
    }

    private void processDirectory(Path path, Map<String, Collection<String>> map) {
        try {
            Files.walk(path)
                    .parallel()
                    .map(path::relativize)
                    .filter(p -> {
                        final Path fileName = p.getFileName();
                        return p.getNameCount() > 1 && fileName != null && fileName.toString().endsWith(".class");
                    })
                    .forEach(p -> {
                        final String pkg = p.getParent().toString().replace(File.separatorChar, '.');
                        final String text = p.getFileName().toString();
                        final String className = text.substring(0, text.length() - ".class".length());
                        map.computeIfAbsent(pkg, v -> new ConcurrentSkipListSet<>()).add(className);
                    });
        } catch (IOException x) {
            warning("Unable to process {0}", x, path);
        }
    }

    protected static class ClassTreeModel implements TreeModel {

        protected final List<String> keys;
        protected final TreeMap<String, List<String>> classMap = new TreeMap<>();
        protected final EventListenerList listenerList = new EventListenerList();

        public ClassTreeModel(Map<String, Collection<String>> map) {
            map.forEach((k, v) -> classMap.put(k, new ArrayList<>(v)));
            keys = new ArrayList<>(classMap.keySet());
        }

        @Override
        public Object getRoot() {
            return "";
        }

        @Override
        public String getChild(Object parent, int index) {
            if (parent == getRoot()) {
                return keys.get(index);
            } else {
                return classMap.get(parent.toString()).get(index);
            }
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent == getRoot()) {
                return keys.size();
            } else {
                return classMap.get(parent.toString()).size();
            }
        }

        @Override
        public boolean isLeaf(Object node) {
            return node != getRoot() && !classMap.containsKey(node.toString());
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (parent == getRoot()) {
                return keys.indexOf(child.toString());
            } else {
                return classMap.get(parent.toString()).indexOf(child.toString());
            }
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listenerList.add(TreeModelListener.class, l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listenerList.remove(TreeModelListener.class, l);
        }
    }
}
