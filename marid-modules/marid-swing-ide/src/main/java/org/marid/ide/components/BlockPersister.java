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

package org.marid.ide.components;

import org.marid.Marid;
import org.marid.bd.Block;
import org.marid.bd.schema.SchemaModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BlockPersister extends Unmarshaller.Listener {

    public static BlockPersister instance;

    private final Class<?>[] classes;
    private final JAXBContext context;

    @Autowired
    public BlockPersister(Set<Block> blocks) {
        final List<Class<?>> classList = new ArrayList<>(blocks.size() + 1);
        classList.add(SchemaModel.class);
        classList.addAll(blocks.stream().map(Block::getClass).collect(Collectors.toSet()));
        this.classes = classList.toArray(new Class<?>[classList.size()]);
        try {
            this.context = JAXBContext.newInstance(classes);
        } catch (JAXBException x) {
            throw new IllegalStateException(x);
        }
        instance = this;
    }

    public void save(Block block, StreamResult streamResult) throws IOException {
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.marshal(block, streamResult);
        } catch (JAXBException x) {
            throw new IOException(x);
        }
    }

    public Block load(InputStream inputStream) throws IOException {
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setListener(this);
            return (Block) unmarshaller.unmarshal(inputStream);
        } catch (JAXBException x) {
            throw new IOException(x);
        }
    }

    public void save(SchemaModel schemaModel, Path path) throws IOException {
        try {
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
            marshaller.marshal(schemaModel, path.toFile());
        } catch (JAXBException x) {
            throw new IOException(x);
        }
    }

    public SchemaModel load(Path path) throws IOException {
        try {
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setListener(this);
            return (SchemaModel) unmarshaller.unmarshal(path.toFile());
        } catch (Exception x) {
            throw new IOException(x);
        }
    }

    @Override
    public void afterUnmarshal(Object target, Object parent) {
        Marid.getCurrentContext().getAutowireCapableBeanFactory().autowireBean(target);
        Marid.getCurrentContext().getAutowireCapableBeanFactory().initializeBean(target, null);
        if (target instanceof Block) {
            ((Block) target).reset();
        }
    }

    @Override
    public String toString() {
        return String.format("%s(%d)", getClass().getSimpleName(), classes.length);
    }
}
