/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.bd.blocks.statements;

import groovy.ui.ConsoleTextEditor;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.marid.bd.*;
import org.marid.bd.blocks.BdBlock;
import org.marid.bd.components.AbstractBlockComponentEditor;
import org.marid.bd.components.StandardBlockComponent;

import javax.swing.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov.
 */
@BdBlock(name = "Script Statement", color = BlockColors.STATEMENTS_BLOCK_COLOR)
@XmlRootElement
public class ScriptStatementBlock extends StandardBlock implements NamedBlock, ConfigurableBlock {

    @XmlElement
    protected String script = "null";

    public final Out singleOut = new Out("out", Statement.class, this::statement);
    public final Out multipleOut = new Out("vector", Statement[].class, this::statements);

    @Override
    public BlockComponent createComponent() {
        return new StandardBlockComponent<>(this, c -> {
            final ConsoleTextEditor consoleTextEditor = new ConsoleTextEditor();
            consoleTextEditor.getTextEditor().setText(script);
            c.add(consoleTextEditor.getTextEditor());
            c.addBlockListener((Listener) script -> {
                if (script != null) {
                    consoleTextEditor.getTextEditor().setText(script);
                }
                c.updateBlock();
                c.getSchemaEditor().repaint();
            });
        });
    }

    private Statement statement() {
        final List<Statement> statements = new AstBuilder().buildFromString(script).stream()
                .filter(n -> n instanceof Statement)
                .map(n -> (Statement) n)
                .collect(Collectors.toList());
        return statements.size() == 1 ? statements.get(0) : new BlockStatement(statements, new VariableScope());
    }

    private Statement[] statements() {
        return new AstBuilder().buildFromString(script).stream()
                .filter(n -> n instanceof Statement)
                .map(n -> (Statement) n)
                .toArray(Statement[]::new);
    }

    @Override
    public Window createWindow(Window parent) {
        final ConsoleTextEditor textEditor = new ConsoleTextEditor();
        textEditor.setShowLineNumbers(true);
        textEditor.setEditable(true);
        textEditor.setName("resizable");
        textEditor.getTextEditor().setText(script);
        return new AbstractBlockComponentEditor<ScriptStatementBlock>(parent, this) {
            {
                tabPane("Script").addLine("Script", textEditor, 1.0);
                afterInit();
            }

            @Override
            protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
                if (!Objects.equals(textEditor.getTextEditor().getText(), script)) {
                    script = textEditor.getTextEditor().getText();
                    fireEvent(Listener.class, l -> l.scriptChanged(script));
                }
            }
        };
    }

    public interface Listener extends EventListener {

        void scriptChanged(String script);
    }
}
