package org.marid.swing;

import images.Images;

import javax.swing.*;

import java.awt.event.ActionEvent;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridAction extends AbstractAction {

    private final MaridActionListener actionListener;

    public MaridAction(String title, String icon, MaridActionListener actionListener) {
        super(s(title));
        this.actionListener = actionListener;
        ImageIcon ic = Images.getIcon(icon);
        if (ic != null) {
            putValue(SMALL_ICON, ic);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionListener.actionPerformed(this, e);
    }

    public interface MaridActionListener {

        void actionPerformed(MaridAction action, ActionEvent actionEvent);
    }
}
