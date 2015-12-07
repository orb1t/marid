/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.ide.cli;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;
import org.marid.collections.history.HistoryNavigator;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.layout.GridBagLayoutSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.swing.Box.Filler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.GridBagConstraints.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class IdeCommandLine extends JXPanel implements GridBagLayoutSupport, PrefSupport, LogSupport, L10nSupport {

    private final CommandLineContext commandLineContext;
    private final CommandLineArea commandLine;
    private final Insets insets = new Insets(5, 10, 5, 10);
    private final HistoryNavigator<String> history = new HistoryNavigator<>(String.class, 1000, s -> isBlank(s) ? null : s.trim());
    private final Filler terminator;
    private final CommandLineBackgroundCache backgroundCache = new CommandLineBackgroundCache();

    @Autowired
    public IdeCommandLine(SpelParserConfiguration spelParserConfiguration, ConfigurableApplicationContext context) {
        super(new GridBagLayout());
        setScrollableHeightHint(ScrollableSizeHint.PREFERRED_STRETCH);
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        commandLineContext = new CommandLineContext(spelParserConfiguration, context);
        add(commandLine = new CommandLineArea(), gbc(REMAINDER, 1, 1, 0, LINE_START, HORIZONTAL, insets, 0, 0));
        add(terminator = (Filler) Box.createVerticalGlue(), gbc(REMAINDER, 1, 1, 1, PAGE_END, VERTICAL, insets, 0, 0));
        commandLine.registerKeyboardAction(e -> execute(), KeyStroke.getKeyStroke("control ENTER"), WHEN_FOCUSED);
        commandLine.registerKeyboardAction(e -> next(), KeyStroke.getKeyStroke("control DOWN"), WHEN_FOCUSED);
        commandLine.registerKeyboardAction(e -> previous(), KeyStroke.getKeyStroke("control UP"), WHEN_FOCUSED);
        commandLine.registerKeyboardAction(e -> reset(), KeyStroke.getKeyStroke("ESCAPE"), WHEN_FOCUSED);
    }

    @PostConstruct
    void loadHistory() {
        history.getHistory().load(preferences());
    }

    @PreDestroy
    void saveHistory() {
        commandLineContext.clean();
        history.getHistory().save(preferences());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics.create();
        try {
            final BufferedImage image = backgroundCache.getImage(getWidth(), getHeight());
            g.drawImage(image, 0, 0, SystemColor.desktop.darker(), this);
        } finally {
            g.dispose();
        }
    }

    protected void addLine(JComponent component) {
        add(component, gbc(REMAINDER, 1, 1, 0, LINE_START, HORIZONTAL, insets, 0, 0), getComponentCount() - 2);
        commandLine.requestFocus();
        scrollRectToVisible(terminator.getBounds());
    }

    protected void execute() {
        if (StringUtils.isBlank(commandLine.getText())) {
            return;
        }
        final AtomicBoolean error = new AtomicBoolean();
        addLine(new CommandLineResult(commandLineContext.evaluate(commandLine.getText(), e -> {
            error.set(true);
            if (e instanceof SpelEvaluationException) {
                final SpelEvaluationException see = (SpelEvaluationException) e;
                commandLine.select(see.getPosition(), commandLine.getText().length());
            }
        })));
        if (!error.get()) {
            history.add(commandLine.getText().trim());
            commandLine.reset();
        }
    }

    protected void previous() {
        final String text = history.getPrevious(() -> commandLine.getText().trim());
        if (text != null) {
            commandLine.setText(text);
        }
    }

    protected void next() {
        final String text = history.getNext();
        if (text != null) {
            commandLine.setText(text);
        }
    }

    protected void reset() {
        final String text = history.reset();
        if (text != null) {
            commandLine.setText(text);
        }
    }
}
