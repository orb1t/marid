/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

import org.marid.Marid;
import org.marid.ide.context.BaseContext;
import org.marid.ide.context.GuiContext;
import org.marid.ide.context.ProfileContext;
import org.marid.lifecycle.MaridRunner;
import org.marid.logging.Logging;
import org.marid.swing.log.SwingHandler;
import org.marid.xml.XmlPersister;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridIde implements MaridRunner {

    public static void main(String[] args) throws Exception {
        Marid.start(EventQueue::invokeLater, args);
    }

    @Override
    public void run(AnnotationConfigApplicationContext context, String... args) throws Exception {
        Logging.rootLogger().addHandler(new SwingHandler());
        context.register(XmlPersister.class, BaseContext.class, ProfileContext.class, GuiContext.class);
    }
}
