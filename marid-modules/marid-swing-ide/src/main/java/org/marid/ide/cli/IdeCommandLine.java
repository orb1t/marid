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
import org.marid.collections.history.HistoryNavigator;
import org.marid.image.MaridIcon;
import org.marid.logging.LogSupport;
import org.marid.pref.PrefSupport;
import org.marid.swing.layout.GridBagLayoutSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static java.awt.GridBagConstraints.*;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class IdeCommandLine extends JPanel implements GridBagLayoutSupport, PrefSupport, LogSupport {

    private final SpelExpressionParser spelExpressionParser;
    private final StandardEvaluationContext evalContext;
    private final CommandLineArea commandLine;
    private final Insets insets = new Insets(5, 10, 5, 10);
    private final JScrollPane scrollPane = new JScrollPane(this, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
    private final HistoryNavigator<String> history = new HistoryNavigator<>(String.class, 1000, s -> isBlank(s) ? null : s.trim());
    private final AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.01f);

    @Autowired
    public IdeCommandLine(SpelParserConfiguration spelParserConfiguration, StandardEvaluationContext context) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        spelExpressionParser = new SpelExpressionParser(spelParserConfiguration);
        evalContext = context;
        setBackground(SystemColor.controlDkShadow);
        add(commandLine = new CommandLineArea(), gbc(REMAINDER, 1, 1, 0, LINE_START, HORIZONTAL, insets, 0, 0));
        add(Box.createVerticalGlue(), gbc(REMAINDER, 1, 1, 1, PAGE_END, VERTICAL, insets, 0, 0));
        commandLine.registerKeyboardAction(this::execute, KeyStroke.getKeyStroke("control ENTER"), WHEN_FOCUSED);
    }

    @PostConstruct
    public void loadHistory() {
        history.getHistory().load(preferences());
    }

    @PreDestroy
    public void saveHistory() {
        history.getHistory().save(preferences());
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setBackground(getBackground());
            g.clearRect(0, 0, getWidth(), getHeight());
            g.setComposite(composite);
            MaridIcon.draw(getWidth(), getHeight(), Color.GREEN, g);
        } finally {
            g.dispose();
        }
    }

    protected void addLine(JComponent component) {
        add(component, gbc(REMAINDER, 1, 1, 0, LINE_START, HORIZONTAL, insets, 0, 0), getComponentCount() - 2);
        scrollPane.getViewport().setViewPosition(new Point(0, Integer.MAX_VALUE));
        commandLine.requestFocus();
    }

    protected void execute(ActionEvent event) {
        if (StringUtils.isBlank(commandLine.getText())) {
            return;
        }
        try {
            final Expression expression = spelExpressionParser.parseExpression(commandLine.getText());
            final Object result = expression.getValue(evalContext);
            if (result instanceof JComponent) {
                addLine((JComponent) result);
            } else {
                final String textResult = (String) evalContext.getTypeConverter().convertValue(
                        result,
                        TypeDescriptor.forObject(result),
                        TypeDescriptor.valueOf(String.class));
                addLine(new CommandLineResult(textResult));
            }
        } catch (Exception x) {

        }
        commandLine.reset();
    }
}
