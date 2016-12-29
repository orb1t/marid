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

package org.marid.dependant.beaneditor;

import javafx.scene.control.ButtonType;
import javafx.util.Pair;
import org.marid.ide.panes.main.IdeToolbar;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.dialog.MaridDialog;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.ButtonType.CANCEL;
import static org.marid.Ide.primaryStage;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({
        BeanEditorTab.class,
        BeanBrowserTable.class,
        BeanListActions.class,
        BeanListTable.class,
        BeanMetaInfoProvider.class
})
public class BeanEditorConfiguration extends DependantConfiguration<BeanEditorParams> {

    @Autowired
    public BeanEditorConfiguration(Environment environment) {
        super(environment);
    }

    @Bean
    public Path beanFilePath() {
        return param().beanFilePath;
    }

    @Bean
    public BeanFile beanFile(Path beanFilePath, ProjectProfile profile) {
        return profile.getBeanFiles()
                .stream()
                .filter(e -> e.getKey().equals(beanFilePath))
                .map(Pair::getValue)
                .findAny()
                .orElse(null);
    }

    @Bean
    @Qualifier("beanList")
    public FxAction browseAction(ObjectProvider<BeanBrowserTable> browserProvider, BeanListActions beanListActions) {
        return new FxAction("browse", "browse", "Edit")
                .bindText("Browse...")
                .setIcon(FontIcon.O_BROWSER)
                .setEventHandler(event -> {
                    final BeanBrowserTable table = browserProvider.getObject();
                    new MaridDialog<List<BeanDefinitionHolder>>(primaryStage, new ButtonType(s("Add"), OK_DONE), CANCEL)
                            .preferredSize(1024, 768)
                            .title("Bean browser")
                            .with((d, p) -> d.setResizable(true))
                            .result(table.getSelectionModel()::getSelectedItems)
                            .with((d, p) -> p.setContent(new MaridScrollPane(table)))
                            .showAndWait()
                            .ifPresent(entries -> entries.forEach(e -> {
                                beanListActions.insertItem(e.getBeanName(), e.getBeanDefinition(), table.metaInfo);
                            }));
                });
    }

    @Bean(initMethod = "run")
    public Runnable toolbarInitializer(IdeToolbar toolbar,
                                       BeanListTable table,
                                       @Qualifier("beanList") Map<String, FxAction> actionMap) {
        return () -> toolbar.on(table, () -> actionMap);
    }
}
