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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.stripToNull;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanData extends AbstractData<BeanData> {

    public final StringProperty type = new SimpleStringProperty(this, "class");
    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty initMethod = new SimpleStringProperty(this, "init-method");
    public final StringProperty destroyMethod = new SimpleStringProperty(this, "destroy-method");
    public final StringProperty factoryBean = new SimpleStringProperty(this, "factory-bean");
    public final StringProperty factoryMethod = new SimpleStringProperty(this, "factory-method");
    public final StringProperty lazyInit = new SimpleStringProperty(this, "lazy-init");

    public final ObservableList<ConstructorArg> constructorArgs = FXCollections.observableArrayList();
    public final ObservableList<Property> properties = FXCollections.observableArrayList();

    public boolean isFactoryBean() {
        return factoryBean.isNotEmpty().get() || factoryMethod.isNotEmpty().get();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(defaultIfBlank(type.get(), ""));
        out.writeUTF(defaultIfBlank(name.get(), ""));
        out.writeUTF(defaultIfBlank(initMethod.get(), ""));
        out.writeUTF(defaultIfBlank(destroyMethod.get(), ""));
        out.writeUTF(defaultIfBlank(factoryBean.get(), ""));
        out.writeUTF(defaultIfBlank(factoryMethod.get(), ""));
        out.writeUTF(defaultIfBlank(lazyInit.get(), ""));

        out.writeInt(constructorArgs.size());
        for (final ConstructorArg arg : constructorArgs) {
            out.writeObject(arg);
        }

        out.writeInt(properties.size());
        for (final Property property : properties) {
            out.writeObject(property);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type.set(stripToNull(in.readUTF()));
        name.set(stripToNull(in.readUTF()));
        initMethod.set(stripToNull(in.readUTF()));
        destroyMethod.set(stripToNull(in.readUTF()));
        factoryBean.set(stripToNull(in.readUTF()));
        factoryMethod.set(stripToNull(in.readUTF()));
        lazyInit.set(stripToNull(in.readUTF()));

        final int argCount = in.readInt();
        for (int i = 0; i < argCount; i++) {
            constructorArgs.add((ConstructorArg) in.readObject());
        }

        final int propCount = in.readInt();
        for (int i = 0; i < propCount; i++) {
            properties.add((Property) in.readObject());
        }
    }
}
