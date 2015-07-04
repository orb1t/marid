/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

import org.marid.dyn.MetaInfo;
import org.marid.ide.frames.MaridFrame;
import org.marid.ide.gui.IdeFrameImpl;
import org.marid.l10n.L10nSupport;
import org.marid.swing.ComponentConfiguration;
import org.marid.swing.actions.ActionKey;
import org.marid.swing.actions.MaridAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class WidgetsMenuRegistry implements L10nSupport {

    private final GenericApplicationContext context;

    @Autowired
    public WidgetsMenuRegistry(GenericApplicationContext context) {
        this.context = context;
    }

    @Autowired
    public void setFrames(ActionMap ideActionMap) {
        for (final String beanName : context.getBeanNamesForType(MaridFrame.class)) {
            final MetaInfo metaInfo = context.findAnnotationOnBean(beanName, MetaInfo.class);
            final String path = metaInfo.path().isEmpty()
                    ? "/Frames//" + beanName
                    : metaInfo.path() + "/" + metaInfo.group() + "/" + beanName;
            ideActionMap.put(new ActionKey(path), new MaridAction(metaInfo.name(), metaInfo.icon(), ev -> {
                final MaridFrame frame = context.getBean(beanName, MaridFrame.class);
                frame.setVisible(true);
            }));
        }
    }

    @Autowired
    public void setPreferences(IdeFrameImpl frame, ActionMap ideActionMap, ComponentConfiguration[] configurations) {
        for (final ComponentConfiguration configuration : configurations) {
            if (configuration.getClass().isAnnotationPresent(MetaInfo.class)) {
                final MetaInfo metaInfo = configuration.getClass().getAnnotation(MetaInfo.class);
                final String id = String.valueOf(configuration.hashCode());
                final String path = metaInfo.path().isEmpty()
                        ? "/Preferences//" + id
                        : metaInfo.path() + "/" + metaInfo.group() + "/" + id;
                ideActionMap.put(new ActionKey(path), new MaridAction(metaInfo.name(), metaInfo.icon(), ev -> {
                    configuration.configurationDialog(frame, s(metaInfo.name())).setVisible(true);
                }));
            }
        }
    }
}
