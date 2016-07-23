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

package org.marid.spring.xml.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.project.ProjectProfile;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Executable;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * @author Dmitry Ovchinnikov.
 */
public class UtilProperties extends AbstractData<UtilProperties> implements BeanLike {

    public final StringProperty id = new SimpleStringProperty(this, "id");
    public final StringProperty valueType = new SimpleStringProperty(this, "value-type", String.class.getName());
    public final StringProperty location = new SimpleStringProperty(this, "location");
    public final StringProperty localOverride = new SimpleStringProperty(this, "local-override");
    public final StringProperty ignoreResourceNotFound = new SimpleStringProperty(this, "ignore-resource-not-found");

    public final ObservableList<Entry> entries = FXCollections.observableArrayList();

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(defaultIfBlank(id.get(), ""));
        out.writeUTF(defaultIfBlank(valueType.get(), ""));
        out.writeUTF(defaultIfBlank(location.get(), ""));
        out.writeUTF(defaultIfBlank(localOverride.get(), ""));
        out.writeUTF(defaultIfBlank(ignoreResourceNotFound.get(), ""));

        out.writeInt(entries.size());
        for (final Entry entry : entries) {
            out.writeObject(entry);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id.set(stripToNull(in.readUTF()));
        valueType.set(stripToNull(in.readUTF()));
        location.set(stripToNull(in.readUTF()));
        localOverride.set(stripToNull(in.readUTF()));
        ignoreResourceNotFound.set(stripToNull(in.readUTF()));

        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final String key = in.readUTF();
            final String value = in.readUTF();
            final Entry entry = new Entry();
            entry.key.set(key);
            entry.value.set(value);
            entries.add(entry);
        }
    }

    @Override
    public Stream<? extends Executable> getConstructors(ProjectProfile profile) {
        return Stream.empty();
    }

    @Override
    public Optional<Class<?>> getClass(ProjectProfile profile) {
        return Optional.of(Properties.class);
    }

    @Override
    public void updateBeanData(ProjectProfile profile) {
    }

    @Override
    public StringProperty nameProperty() {
        return id;
    }
}
