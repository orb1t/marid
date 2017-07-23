package org.marid.dependant.beaneditor;

import javafx.collections.ObservableList;
import org.marid.ide.model.BeanMethodData;
import org.marid.jfx.table.MaridTableView;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanInitializersTable extends MaridTableView<BeanMethodData> {

    public BeanInitializersTable(ObservableList<BeanMethodData> list) {
        super(list);
    }
}
