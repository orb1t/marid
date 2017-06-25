package org.marid.dependant.monitor;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.marid.ide.tabs.IdeTab;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@EnableScheduling
@Import({ClassLoadingWidget.class, MemoryWidget.class, OperatingSystemWidget.class, ThreadWidget.class})
public class MonitorConfiguration {

    @Bean
    public GridPane monitorGridPane() {
        final GridPane pane = new GridPane();

        final ColumnConstraints col1 = new ColumnConstraints();
        col1.setFillWidth(true);
        col1.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().add(col1);

        final RowConstraints row1 = new RowConstraints();
        row1.setFillHeight(true);
        row1.setVgrow(Priority.ALWAYS);
        pane.getRowConstraints().add(row1);

        final ColumnConstraints col2 = new ColumnConstraints();
        col2.setFillWidth(true);
        col2.setHgrow(Priority.ALWAYS);
        pane.getColumnConstraints().add(col2);

        final RowConstraints row2 = new RowConstraints();
        row2.setFillHeight(true);
        row2.setVgrow(Priority.ALWAYS);
        pane.getRowConstraints().add(row2);
        return pane;
    }

    @Bean
    public IdeTab tab(GridPane monitorGridPane) {
        return new IdeTab(monitorGridPane, ls("Monitor"), () -> glyphIcon("O_GRAPH", 16));
    }
}
