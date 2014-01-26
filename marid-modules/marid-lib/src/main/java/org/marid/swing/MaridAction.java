package org.marid.swing;

import images.Images;

import javax.swing.*;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class MaridAction extends AbstractAction {

    public MaridAction() {
        super();
    }

    public MaridAction(String title) {
        super(s(title));
    }

    public MaridAction(String title, String icon) {
        super(s(title));
        ImageIcon ic = Images.getIcon(icon);
        if (ic != null) {
            putValue(SMALL_ICON, ic);
        }
    }

    public MaridAction(String title, Icon icon) {
        super(s(title), icon);
    }
}
