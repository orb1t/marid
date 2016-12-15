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

package org.marid.hmi.screen;

import org.marid.beans.MaridBeanInfo;
import org.marid.editors.hmi.screen.ScreenEditor;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class HmiScreenBeanInfo extends MaridBeanInfo {

    public HmiScreenBeanInfo() {
        super(HmiScreen.class, ScreenEditor.class);
        descriptor.setShortDescription("HMI Screen based on a SVG file/resource");
        descriptor.setDisplayName("HMI Screen");
        descriptor.setValue("icon16", getClass().getResource("screen16.png"));
        descriptor.setValue("icon32", getClass().getResource("screen32.png"));
    }

    @Override
    public BeanInfo[] getAdditionalBeanInfo() {
        return new BeanInfo[] {
                new SimpleBeanInfo() {

                    private final PropertyDescriptor[] propertyDescriptors = new PropertyDescriptorsBuilder()
                            .add("relativeUrl", descriptor -> {
                                descriptor.setPreferred(true);
                                descriptor.setPropertyEditorClass(ScreenEditor.class);
                            })
                            .build();

                    @Override
                    public PropertyDescriptor[] getPropertyDescriptors() {
                        return propertyDescriptors;
                    }
                }
        };
    }
}
