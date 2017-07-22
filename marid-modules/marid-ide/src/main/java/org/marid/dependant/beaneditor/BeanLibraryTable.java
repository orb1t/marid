package org.marid.dependant.beaneditor;

import javafx.collections.FXCollections;
import org.marid.ide.model.BeanMethodData;
import org.marid.jfx.action.SpecialActions;
import org.marid.jfx.table.MaridTableView;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class BeanLibraryTable extends MaridTableView<BeanMethodData> {

    @Autowired
    public BeanLibraryTable(SpecialActions specialActions) {
        super(FXCollections.emptyObservableList(), specialActions);
    }
}
