package org.marid.swing;

import images.Images;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.methods.LogMethods;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridAction extends AbstractAction implements L10nSupport {

    private static final Logger LOG = Logger.getLogger(MaridAction.class.getName());
    private final MaridActionListener actionListener;

    public MaridAction(String title, String icon, MaridActionListener actionListener, Object... args) {
        super(LS.s(title));
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
            final ImageIcon ic = Images.getIcon(icon, 16);
            if (ic != null) {
                putValue(SMALL_ICON, ic);
            }
        }
        for (int i = 0; i < args.length; i += 2) {
            if (args[i + 1] != null) {
                putValue(args[i].toString(), args[i + 1]);
            }
        }
        if (getValue(Action.SHORT_DESCRIPTION) == null) {
            putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        }
    }

    public MaridAction(String title, String icon, ActionListener actionListener, Object... args) {
        this(title, icon, (a, e) -> actionListener.actionPerformed(e), args);
    }

    public MaridAction setKey(String key) {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
        return this;
    }

    public MaridAction setShortDescription(String description, Object... args) {
        putValue(SHORT_DESCRIPTION, s(description, args));
        return this;
    }

    public MaridAction setLongDescription(String description, Object... args) {
        putValue(LONG_DESCRIPTION, s(description, args));
        return this;
    }

    public MaridAction setValue(String key, Object value) {
        putValue(key, value);
        return this;
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

        void actionPerformed(Action action, ActionEvent actionEvent) throws Exception;
    }
}
