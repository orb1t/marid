package org.marid.swing;

import images.Images;
import org.marid.l10n.Localized;

import javax.swing.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class MaridAction extends AbstractAction implements Localized {

    public MaridAction() {
        super();
    }

    public MaridAction(String title) {
        super(S.l(title));
    }

    public MaridAction(String title, String icon) {
        super(S.l(title));
        ImageIcon ic = Images.getIcon(icon);
        if (ic != null) {
            putValue(SMALL_ICON, ic);
        }
    }

    public MaridAction(String title, Icon icon) {
        super(S.l(title), icon);
    }
}
