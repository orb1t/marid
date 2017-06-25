package org.marid.jfx.list;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class EmptyListChange<E> extends ListChangeListener.Change<E> {

    public EmptyListChange(ObservableList<E> list) {
        super(list);
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public int getFrom() {
        return 0;
    }

    @Override
    public int getTo() {
        return 0;
    }

    @Override
    public List<E> getRemoved() {
        return Collections.emptyList();
    }

    @Override
    protected int[] getPermutation() {
        return new int[0];
    }
}
