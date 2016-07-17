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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFile extends AbstractData<BeanFile> {

    public final ObservableList<BeanData> beans = FXCollections.observableArrayList();
    public final ObservableList<UtilProperties> properties = FXCollections.observableArrayList();
    public final ObservableList<UtilConstant> constants = FXCollections.observableArrayList();

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(beans.size());
        for (final BeanData beanData : beans) {
            out.writeObject(beanData);
        }

        out.writeInt(properties.size());
        for (final UtilProperties prop : properties) {
            out.writeObject(prop);
        }

        out.writeInt(constants.size());
        for (final UtilConstant constant : constants) {
            out.writeObject(constant);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        final int beanCount = in.readInt();
        for (int i = 0; i < beanCount; i++) {
            beans.add((BeanData) in.readObject());
        }

        final int propCount = in.readInt();
        for (int i = 0; i < propCount; i++) {
            properties.add((UtilProperties) in.readObject());
        }

        final int constCount = in.readInt();
        for (int i = 0; i < constCount; i++) {
            constants.add((UtilConstant) in.readObject());
        }
    }
}
