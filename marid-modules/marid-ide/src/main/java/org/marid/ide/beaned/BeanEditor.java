/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.beaned;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.marid.ide.Ide;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class BeanEditor extends Stage implements L10nSupport, LogSupport {

    @Inject
    public BeanEditor(BeanEditorPane beanEditorPane) {
        setMaximized(true);
        getIcons().addAll(Ide.IMAGES);
        setScene(new Scene(beanEditorPane, 1024, 768));
        setTitle("[" + beanEditorPane.profile + "] " + s("New"));
        setOnCloseRequest(event -> {
            try {
                beanEditorPane.classLoader.close();
            } catch (Exception x) {
                log(WARNING, "Unable to free resources", x);
            }
        });
    }
}
