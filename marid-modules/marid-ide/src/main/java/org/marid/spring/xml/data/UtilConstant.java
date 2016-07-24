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
import org.marid.ide.project.ProjectProfile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.marid.spring.xml.MaridBeanDefinitionSaver.SPRING_SCHEMA_PREFIX;
import static org.marid.spring.xml.MaridBeanUtils.setAttr;

/**
 * @author Dmitry Ovchinnikov.
 */
public class UtilConstant extends AbstractData<UtilConstant> implements BeanLike {

    public final StringProperty id = new SimpleStringProperty(this, "id");
    public final StringProperty staticField = new SimpleStringProperty(this, "static-field");

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(defaultIfBlank(id.get(), ""));
        out.writeUTF(defaultIfBlank(staticField.get(), ""));
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id.set(stripToNull(in.readUTF()));
        staticField.set(stripToNull(in.readUTF()));
    }

    @Override
    public Stream<? extends Executable> getConstructors(ProjectProfile profile) {
        return Stream.empty();
    }

    @Override
    public Optional<Class<?>> getClass(ProjectProfile profile) {
        if (staticField.isEmpty().get()) {
            return Optional.empty();
        }
        final String text = staticField.get();
        final int index = text.lastIndexOf('.');
        if (index < 0) {
            return Optional.empty();
        }
        final String className = text.substring(0, index);
        final String fieldName = text.substring(index + 1);
        final Optional<Class<?>> type = profile.getClass(className);
        if (type.isPresent()) {
            try {
                final Field field = type.get().getField(fieldName);
                return Optional.of(field.getType());
            } catch (NoSuchFieldException x) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void updateBeanData(ProjectProfile profile) {
    }

    @Override
    public StringProperty nameProperty() {
        return id;
    }

    @Override
    public void save(Node node, Document document) {
        final Element beanElement = document.createElementNS(SPRING_SCHEMA_PREFIX + "util", "util:constant");
        node.appendChild(beanElement);
        setAttr(id, beanElement);
        setAttr(staticField, beanElement);
    }

    @Override
    public void load(Node node, Document document) {

    }
}
