/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.widgets.services;

import images.Images;
import org.marid.dyn.MetaInfo;
import org.marid.functions.SafeBiFunction;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.service.MaridService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServicesTableModel extends AbstractTableModel implements LogSupport {

    protected static final String RESOURCE_NAME = "META-INF/services/" + MaridService.class.getName();
    protected static final String[] COLUMN_NAMES;
    protected static final Class<?>[] COLUMN_CLASSES;
    protected static final SafeBiFunction<Object, Integer, Object> DATA_FUNCTION;

    static {
        final List<Method> methods = Arrays.stream(Service.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(MetaInfo.class))
                .peek(m -> m.setAccessible(true))
                .sorted(Comparator.comparing(m -> m.getAnnotation(MetaInfo.class).order()))
                .collect(Collectors.toList());
        COLUMN_NAMES = methods.stream().map(m -> m.getAnnotation(MetaInfo.class).name()).toArray(String[]::new);
        COLUMN_CLASSES = methods.stream().map(Method::getReturnType).toArray(Class<?>[]::new);
        DATA_FUNCTION = (d, i) -> methods.get(i).invoke(d);
    }

    private final List<Service> services = new ArrayList<>();

    public ServicesTableModel(ClassLoader classLoader) {
        try {
            for (final Enumeration<URL> e = classLoader.getResources(RESOURCE_NAME); e.hasMoreElements(); ) {
                final URL url = e.nextElement();
                try (final InputStream is = url.openStream(); final Scanner scanner = new Scanner(is, "UTF-8")) {
                    while (scanner.hasNextLine()) {
                        final String line = scanner.nextLine().trim();
                        if (line.startsWith("#") || line.isEmpty()) {
                            continue;
                        }
                        try {
                            final Class<?> c = classLoader.loadClass(line);
                            if (c.isAnnotationPresent(MetaInfo.class)) {
                                services.add(new Service(c));
                            }
                        } catch (Exception x) {
                            warning("Unable to load class {0}", x, line);
                        }
                    }
                }
            }
            final Comparator<Service> cmp = Comparator.comparing(s -> s.type.getAnnotation(MetaInfo.class).order());
            Collections.sort(services, cmp.thenComparing(s -> s.name));
        } catch (Exception x) {
            warning("Unable to enumerate services", x);
        }
    }

    @Override
    public int getRowCount() {
        return services.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return DATA_FUNCTION.apply(services.get(rowIndex), columnIndex);
    }

    protected static class Service implements L10nSupport {

        protected final ImageIcon icon;
        protected final String name;
        protected final Class<?> type;
        protected final String description;

        protected String alias;
        protected String qualifier;

        public Service(Class<?> serviceClass) {
            final MetaInfo metaInfo = serviceClass.getAnnotation(MetaInfo.class);
            icon = Images.getIcon("services/" + metaInfo.icon(), 24);
            name = s(metaInfo.name());
            description = s(metaInfo.description());
            type = serviceClass;
        }

        public ImageIcon icon() {
            return icon;
        }

        @MetaInfo(name = "Name", order = 1)
        public String name() {
            return name;
        }

        @MetaInfo(name = "Alias", order = 2)
        public String alias() {
            return alias;
        }

        @MetaInfo(name = "Qualifier", order = 3)
        public String qualifier() {
            return qualifier;
        }

        @MetaInfo(name = "Type", order = 4)
        public Class<?> type() {
            return type;
        }

        @MetaInfo(name = "Description", order = 5)
        public String description() {
            return description;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
