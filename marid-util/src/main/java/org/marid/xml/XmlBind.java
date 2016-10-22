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

package org.marid.xml;

import org.marid.cache.MaridClassValue;
import org.marid.function.SafeBiFunction;
import org.marid.function.SafeTriConsumer;
import org.marid.misc.Calls;
import org.marid.misc.Casts;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.function.Consumer;

import static org.marid.misc.Calls.call;

/**
 * @author Dmitry Ovchinnikov
 */
public class XmlBind {

    public static final ClassValue<JAXBContext> JAXB = new MaridClassValue<>(c -> () -> JAXBContext.newInstance(c));

    public static final Consumer<Marshaller> DEFAULT_OUTPUT = marshaller -> Calls.call(() -> {
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        return null;
    });
    public static final Consumer<Marshaller> FORMATTED_OUTPUT = DEFAULT_OUTPUT.andThen(marshaller -> Calls.call(() -> {
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return null;
    }));
    public static final Consumer<Unmarshaller> DEFAULT_INPUT = unmarshaller -> {
    };

    public static <T, R> void save(Class<?> type,
                                   R bean,
                                   T output,
                                   Consumer<Marshaller> marshallerConsumer,
                                   SafeTriConsumer<Marshaller, R, T> marshalTask) {
        final JAXBContext context = JAXB.get(type);
        final Marshaller marshaller = call(context::createMarshaller);
        marshallerConsumer.accept(marshaller);
        marshalTask.accept(marshaller, bean, output);
    }

    public static <T, R> R load(Class<?> type,
                                T input,
                                Consumer<Unmarshaller> unmarshallerConsumer,
                                SafeBiFunction<Unmarshaller, T, Object> unmarshalTask) {
        final JAXBContext context = JAXB.get(type);
        final Unmarshaller unmarshaller = call(context::createUnmarshaller);
        unmarshallerConsumer.accept(unmarshaller);
        return Casts.cast(unmarshalTask.apply(unmarshaller, input));
    }

    public static <T, R> void save(R bean,
                                   T output,
                                   Consumer<Marshaller> marshallerConsumer,
                                   SafeTriConsumer<Marshaller, R, T> marshalTask) {
        save(bean.getClass(), bean, output, marshallerConsumer, marshalTask);
    }

    public static <T, R> void save(R bean, T output, SafeTriConsumer<Marshaller, R, T> marshalTask) {
        save(bean, output, FORMATTED_OUTPUT, marshalTask);
    }

    public static <T, R> R loadBean(Class<R> type,
                                    T input,
                                    Consumer<Unmarshaller> unmarshallerConsumer,
                                    SafeBiFunction<Unmarshaller, T, Object> unmarshalTask) {
        return load(type, input, unmarshallerConsumer, unmarshalTask);
    }

    public static <T, R> R load(Class<R> type, T input, SafeBiFunction<Unmarshaller, T, Object> unmarshalTask) {
        return loadBean(type, input, DEFAULT_INPUT, unmarshalTask);
    }
}
