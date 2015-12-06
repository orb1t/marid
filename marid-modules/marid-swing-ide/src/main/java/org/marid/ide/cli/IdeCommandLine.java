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
import org.marid.swing.ComponentWrapper;
import org.marid.swing.layout.GridBagLayoutSupport;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import javax.swing.Box.Filler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import static java.awt.GridBagConstraints.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class IdeCommandLine extends JXPanel implements GridBagLayoutSupport, PrefSupport, LogSupport, L10nSupport {

    private final SpelExpressionParser spelExpressionParser;
    private final StandardEvaluationContext evalContext;
    private final CommandLineArea commandLine;
    private final Insets insets = new Insets(5, 10, 5, 10);
    private final HistoryNavigator<String> history = new HistoryNavigator<>(String.class, 1000, s -> isBlank(s) ? null : s.trim());
    private final Filler terminator;
    private final CommandLineBackgroundCache backgroundCache = new CommandLineBackgroundCache();

    @Autowired
    public IdeCommandLine(SpelParserConfiguration spelParserConfiguration, StandardEvaluationContext context) {
        super(new GridBagLayout());
        setScrollableHeightHint(ScrollableSizeHint.PREFERRED_STRETCH);
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        spelExpressionParser = new SpelExpressionParser(spelParserConfiguration);
        evalContext = context;
        add(commandLine = new CommandLineArea(), gbc(REMAINDER, 1, 1, 0, LINE_START, HORIZONTAL, insets, 0, 0));
        add(terminator = (Filler) Box.createVerticalGlue(), gbc(REMAINDER, 1, 1, 1, PAGE_END, VERTICAL, insets, 0, 0));
        commandLine.registerKeyboardAction(e -> execute(), KeyStroke.getKeyStroke("control ENTER"), WHEN_FOCUSED);
        commandLine.registerKeyboardAction(e -> next(), KeyStroke.getKeyStroke("control DOWN"), WHEN_FOCUSED);
        commandLine.registerKeyboardAction(e -> previous(), KeyStroke.getKeyStroke("control UP"), WHEN_FOCUSED);
        commandLine.registerKeyboardAction(e -> reset(), KeyStroke.getKeyStroke("ESCAPE"), WHEN_FOCUSED);
    }

    @PostConstruct
    public void loadHistory() {
        history.getHistory().load(preferences());
    }

    @PreDestroy
    public void saveHistory() {
        history.getHistory().save(preferences());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setBackground(SystemColor.desktop.darker());
            g.clearRect(0, 0, getWidth(), getHeight());
            final BufferedImage image = backgroundCache.getImage(getWidth(), getHeight());
            g.drawImage(image, 0, 0, this);
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
        String text = null;
        JComponent component = null;
        try {
            final Expression expression = spelExpressionParser.parseExpression(commandLine.getText());
            final Object result = expression.getValue(evalContext);
            if (result instanceof ComponentWrapper) {
                component = ((ComponentWrapper) result).getWrappedComponent();
            } else {
                final TypeDescriptor source = TypeDescriptor.forObject(result);
                final TypeDescriptor target = TypeDescriptor.valueOf(String.class);
                if (evalContext.getTypeConverter().canConvert(source, target)) {
                    text = (String) evalContext.getTypeConverter().convertValue(result, source, target);
                } else if (result.getClass().isArray()) {
                    text = Arrays.deepToString((Object[]) result);
                } else {
                    text = result.toString();
                }
            }
            history.add(commandLine.getText().trim());
            commandLine.reset();
        } catch (Exception x) {
            for (Throwable t = x; t != null; t = t.getCause()) {
                if (t instanceof NoSuchBeanDefinitionException) {
                    final NoSuchBeanDefinitionException exception = (NoSuchBeanDefinitionException) t;
                    text = m("No such bean {0}", exception.getBeanName());
                    break;
                }
            }
            if (text == null) {
                final StringWriter sw = new StringWriter();
                try (final PrintWriter w = new PrintWriter(sw)) {
                    x.printStackTrace(w);
                }
                text = sw.toString();
            }
            if (x instanceof SpelEvaluationException) {
                final SpelEvaluationException see = (SpelEvaluationException) x;
                commandLine.select(see.getPosition(), commandLine.getText().length());
            }
        }
        if (component != null) {
            addLine(new CommandLineResult(component));
        } else if (text != null) {
            addLine(new CommandLineResult(new CommandLineResultArea(text)));
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
