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

package org.marid.servcon.model;

import org.marid.dyn.MetaInfo;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static java.util.Arrays.stream;
import static org.marid.reflect.IntrospectionUtils.getPropertyDescriptors;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ClassBlock extends Block {

    private final Class<?> object;
    private transient final Param[] params;
    private transient final In[] inputs;
    private transient final Out[] outputs;

    public ClassBlock(Class<?> type) {
        this.object = type;
        this.params = params();
        this.inputs = in();
        this.outputs = out();
    }

    private PropertyDescriptor[] pds() {
        return getPropertyDescriptors(object);
    }

    private Param[] params() {
        final Constructor<?>[] constructors = object.getConstructors();
        return constructors.length == 0
                ? new Param[0]
                : stream(constructors[0].getParameters()).map(ClassParam::new).toArray(Param[]::new);
    }

    private In[] in() {
        return stream(pds()).filter(p -> p.getWriteMethod() != null).map(ClassIn::new).toArray(In[]::new);
    }

    private Out[] out() {
        return stream(pds()).filter(p -> !p.getName().equals("class") && p.getReadMethod() != null)
                .map(ClassOut::new).toArray(Out[]::new);
    }

    @Override
    public Class<?> getType() {
        return object;
    }

    @Override
    public Class<?> getObject() {
        return object;
    }

    @Override
    public Param[] getParameters() {
        return params;
    }

    @Override
    public In[] getInputs() {
        return inputs;
    }

    @Override
    public Out[] getOutputs() {
        return outputs;
    }

    @Override
    public String getName() {
        return getMetaInfo().name().isEmpty() ? object.getSimpleName() : getMetaInfo().name();
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();
        try {
            final Field inputsField = ClassBlock.class.getDeclaredField("inputs");
            inputsField.setAccessible(true);
            inputsField.set(this, in());
            final Field outputsField = ClassBlock.class.getDeclaredField("outputs");
            outputsField.setAccessible(true);
            outputsField.set(this, out());
            final Field paramsField = ClassBlock.class.getDeclaredField("params");
            paramsField.setAccessible(true);
            paramsField.set(this, params());
        } catch (Exception x) {
            throw new IOException(x);
        }
    }

    private class ClassParam extends Param {

        private final Parameter parameter;

        public ClassParam(Parameter parameter) {
            this.parameter = parameter;
        }

        @Override
        public Class<?> getType() {
            return parameter.getType();
        }

        @Override
        public String getName() {
            return parameter.getName();
        }

        @Override
        public MetaInfo getMetaInfo() {
            final MetaInfo metaInfo = parameter.getAnnotation(MetaInfo.class);
            return metaInfo == null ? super.getMetaInfo() : metaInfo;
        }
    }

    private class ClassIn extends In {

        private final Method method;
        private final String name;

        public ClassIn(PropertyDescriptor propertyDescriptor) {
            this.method = propertyDescriptor.getWriteMethod();
            this.name = propertyDescriptor.getName();
        }

        @Override
        public Class<?> getType() {
            return method.getParameterTypes()[0];
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public MetaInfo getMetaInfo() {
            final MetaInfo metaInfo = method.getAnnotation(MetaInfo.class);
            return metaInfo == null ? super.getMetaInfo() : metaInfo;
        }
    }

    private class ClassOut extends Out {

        private final Method method;
        private final String name;

        public ClassOut(PropertyDescriptor propertyDescriptor) {
            this.method = propertyDescriptor.getReadMethod();
            this.name = propertyDescriptor.getName();
        }

        @Override
        public Class<?> getType() {
            return method.getReturnType();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public MetaInfo getMetaInfo() {
            final MetaInfo metaInfo = method.getAnnotation(MetaInfo.class);
            return metaInfo == null ? super.getMetaInfo() : metaInfo;
        }
    }
}
