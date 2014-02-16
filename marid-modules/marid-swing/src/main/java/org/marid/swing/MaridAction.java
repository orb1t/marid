package org.marid.swing;

import images.Images;
import org.marid.logging.LogSupport;
import org.marid.methods.LogMethods;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(MaridAction.class.getName());
    private final MaridActionListener actionListener;

    public MaridAction(String title, String icon, MaridActionListener actionListener, Object... args) {
        super(s(title));
        this.actionListener = actionListener;
        if (icon != null && icon.indexOf('.') < 0) {
            final ImageIcon smallIcon = Images.getIcon(icon + "16.png");
            if (smallIcon != null) {
                putValue(SMALL_ICON, smallIcon);
            }
            final ImageIcon largeIcon = Images.getIcon(icon + "24.png");
            if (largeIcon != null) {
                putValue(LARGE_ICON_KEY, largeIcon);
            }
            if (smallIcon == null) {
                final ImageIcon imageIcon = Images.getIcon(icon + ".png");
                if (imageIcon != null) {
                    putValue(SMALL_ICON, imageIcon);
                }
            }
        } else {
            final ImageIcon ic = Images.getIcon(icon);
            if (ic != null) {
                putValue(SMALL_ICON, ic);
            }
        }
        for (int i = 0; i < args.length; i += 2) {
            putValue(args[i].toString(), args[i + 1]);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            actionListener.actionPerformed(this, e);
        } catch (Exception x) {
            if (e.getSource() instanceof Component) {
                final Window window = SwingUtilities.windowForComponent((Component) e.getSource());
                if (window instanceof LogSupport) {
                    ((LogSupport) window).warning("Action {0} error", x, getValue(NAME));
                    return;
                }
            }
            LogMethods.warning(LOG, "Action {0} error", x, getValue(NAME));
        }
    }

    public interface MaridActionListener {

        void actionPerformed(MaridAction action, ActionEvent actionEvent) throws Exception;
    }
}
