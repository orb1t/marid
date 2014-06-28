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

package org.marid.ide;

import org.marid.bd.schema.SchemaFrameConfiguration;
import org.marid.ide.bde.BdeConfiguration;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.ConfigurationProvider;

import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultConfigurationProvider implements ConfigurationProvider {
    @Override
    public void visitConfigurationClasses(Consumer<Class<? extends Configuration>> consumer) {
        consumer.accept(BdeConfiguration.class);
        consumer.accept(SchemaFrameConfiguration.class);
    }
}
