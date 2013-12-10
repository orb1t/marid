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

package org.marid.ide.swing.impl;

import org.marid.ide.itf.Desktop;
import org.marid.ide.swing.impl.widgets.ConsoleImpl;
import org.marid.ide.swing.impl.widgets.ResizableWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Desktop implementation.
 *
 * @author Dmitry Ovchinnikov
 */
public class DesktopImpl extends JDesktopPane implements Desktop {

    private final ConsoleImpl console;

    public DesktopImpl() {
        setDesktopManager(new DesktopManagerImpl());
        add(console = new ConsoleImpl());
        console.setVisible(true);
        addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                if (e.getChild() instanceof JInternalFrame) {
                    e.getChild().addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentShown(ComponentEvent e) {
                            getDesktopManager().openFrame((JInternalFrame) e.getComponent());
                            e.getComponent().removeComponentListener(this);
                        }
                    });
                }
            }
        });
    }

    @Override
    public DesktopManagerImpl getDesktopManager() {
        return (DesktopManagerImpl) super.getDesktopManager();
    }

    @Override
    public ConsoleImpl getConsole() {
        return console;
    }

    public class DesktopManagerImpl extends DefaultDesktopManager {

        @Override
        public void openFrame(JInternalFrame f) {
            super.openFrame(f);
            checkBounds(f);
        }

        @Override
        public void beginResizingFrame(JComponent f, int direction) {
            super.beginResizingFrame(f, direction);
            if (f instanceof ResizableWidget) {
                ((ResizableWidget) f).beginResizing();
            }
        }

        @Override
        public void endResizingFrame(JComponent f) {
            super.endResizingFrame(f);
            if (f instanceof ResizableWidget) {
                ((ResizableWidget) f).endResizing();
            }
        }

        @Override
        public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
            super.resizeFrame(f, newX, newY, newWidth, newHeight);
            if (f instanceof ResizableWidget) {
                ((ResizableWidget) f).onResize();
            }
        }

        @Override
        public void dragFrame(JComponent f, int newX, int newY) {
            super.dragFrame(f, newX, newY);
        }

        private void checkBounds(JInternalFrame f) {
            int x = f.getX() < 0 ? 0 : f.getX();
            int y = f.getY() < 0 ? 0 : f.getY();
            int w = f.getWidth();
            int h = f.getHeight();
            if (x + w > getWidth()) {
                if (w > getWidth()) {
                    x = 0;
                    w = getWidth();
                } else {
                    x = getWidth() - w;
                }
            }
            if (y + h > getHeight()) {
                if (h > getHeight()) {
                    y = 0;
                    h = getHeight();
                } else {
                    y = getHeight() - h;
                }
            }
            Rectangle bounds = new Rectangle(x, y, w, h);
            if (!bounds.equals(f.getBounds())) {
                f.setBounds(bounds);
            }
        }
    }
}
