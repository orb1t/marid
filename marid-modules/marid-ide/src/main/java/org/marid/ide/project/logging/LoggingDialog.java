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

package org.marid.ide.project.logging;

import javafx.scene.control.ButtonType;
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.dialog.MaridDialog;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class LoggingDialog extends MaridDialog<Runnable> {

    private final ProjectProfile profile;
    private final Properties properties = new Properties();

    @Inject
    public LoggingDialog(IdePane idePane, ProjectProfile profile) {
        super(idePane, ButtonType.APPLY, ButtonType.CANCEL);
        this.profile = profile;
    }
}
