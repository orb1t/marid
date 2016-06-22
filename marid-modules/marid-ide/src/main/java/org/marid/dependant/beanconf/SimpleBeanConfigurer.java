package org.marid.dependant.beanconf;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.ide.panes.main.IdePane;
import org.marid.l10n.L10nSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SimpleBeanConfigurer extends Stage implements L10nSupport {

    @Autowired
    public SimpleBeanConfigurer(IdePane idePane, SimpleBeanConfigurerContext context) {
        super(StageStyle.UTILITY);
        initOwner(idePane.getScene().getWindow());
    }
}
