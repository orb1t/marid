package org.marid.collections.history;

import java.util.function.UnaryOperator;

/**
 * @author Dmitry Ovchinnikov
 */
public class HistoryNavigator<T> {

    final History<T> history;

    private int currentIndex = -1;

    public HistoryNavigator(History<T> history) {
        this.history = history;
    }

    public HistoryNavigator(Class<T> elementType, int maxItems, UnaryOperator<T> addOp) {
        this(new History<T>(elementType, maxItems, addOp));
    }

    public void add(T element) {
        history.add(element);
        reset();
    }

    public T getPrevious() {
        if (history.getSize() == 0) {
            return null;
        } else if (currentIndex < 0 || currentIndex >= history.getSize() - 1) {
            return history.getHistoryItem(currentIndex = 0);
        } else {
            return history.getHistoryItem(++currentIndex);
        }
    }

    public T getNext() {
        if (history.getSize() == 0) {
            return null;
        } else if (currentIndex <= 0) {
            return history.getHistoryItem(currentIndex = history.getSize() - 1);
        } else {
            return history.getHistoryItem(--currentIndex);
        }
    }

    public void reset() {
        currentIndex = -1;
    }

    public History<T> getHistory() {
        return history;
    }
}
