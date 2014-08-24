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

package org.marid.ide.swing.context;

import org.marid.ide.base.Ide;
import org.marid.ide.base.IdeFrame;
import org.marid.ide.swing.IdeFrameImpl;
import org.marid.ide.swing.IdeImpl;
import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.marid.swing.SwingUtil;
import org.marid.swing.menu.MenuActionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class GuiContext implements LogSupport, SysPrefSupport {

    @Autowired
    private MenuActionList menuActionList;

    public GuiContext() {
        SwingUtil.execute(() -> {
            final String laf = getSysPref("laf", "");
            try {
                if (!laf.isEmpty()) {
                    UIManager.setLookAndFeel(laf);
                } else {
                    UIManager.setLookAndFeel(new NimbusLookAndFeel());
                }
            } catch (Exception x) {
                warning("Unable to set LAF {0}", x, laf);
            }
        });
    }

    @PostConstruct
    public void init() {
        ideFrame().setVisible(true);
    }

    @Bean
    public Ide ide() {
        return new IdeImpl();
    }

    @Bean(destroyMethod = "dispose")
    public IdeFrame ideFrame() {
        return new IdeFrameImpl(ide(), true, menuActionList.createTreeElement());
    }
}
