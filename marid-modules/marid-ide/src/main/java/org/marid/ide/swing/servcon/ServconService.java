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

package org.marid.ide.swing.servcon;

import images.Images;
import org.marid.l10n.L10n;
import org.marid.service.MaridService;
import org.marid.service.ServiceDescriptor;
import org.marid.swing.dnd.DndObject;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ServconService implements DndObject, Comparable<ServconService> {

    private final Class<? extends MaridService> serviceType;
    private final ImageIcon icon;
    private final String name;

    public ServconService(Class<? extends MaridService> serviceType) {
        this.serviceType = serviceType;
        final ServiceDescriptor serviceDescriptor = serviceType.getAnnotation(ServiceDescriptor.class);
        final ImageIcon imageIcon;
        if (serviceDescriptor != null) {
            this.name = serviceDescriptor.name();
            imageIcon = Images.getIcon(serviceDescriptor.icon(), 32);
        } else {
            this.name = serviceType.getSimpleName();
            imageIcon = Images.getIcon("services/service.png", 32);
        }
        this.icon = imageIcon == null ? new ImageIcon(new BufferedImage(32, 32, TYPE_INT_ARGB)) : imageIcon;
    }

    @Override
    public Class<? extends MaridService> getObject() {
        return serviceType;
    }

    @Override
    public ImageIcon getVisualRepresentation() {
        return icon;
    }

    @Override
    public int compareTo(@Nonnull ServconService that) {
        return name.compareTo(that.name);
    }

    @Override
    public String toString() {
        return L10n.s(name);
    }
}
