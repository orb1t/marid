/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.dependant.beaneditor;

import com.github.javaparser.ast.body.MethodDeclaration;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.event.TextFileChangedEvent;
import org.marid.ide.event.TextFileMovedEvent;
import org.marid.ide.model.TextFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Dmitry Ovchinnikov
 */
@Service
public class BeanEditorUpdater {

    private final ObservableList<MethodDeclaration> methodDeclarations = FXCollections.observableArrayList();
    private final TextFile textFile;

    @Autowired
    public BeanEditorUpdater(TextFile textFile) {
        this.textFile = textFile;
    }

    @PostConstruct
    public void update() {

    }

    @EventListener(condition = "@javaFile.equals(#event.source)")
    public void onMove(TextFileMovedEvent event) {
        Platform.runLater(() -> {
            textFile.setPath(event.getTarget());
            update();
        });
    }

    @EventListener(condition = "@javaFile.equals(#event.source)")
    public void onChange(TextFileChangedEvent event) {
        Platform.runLater(this::update);
    }
}
