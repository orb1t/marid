package org.marid.collections.history;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @author Dmitry Ovchinnikov
 */
public class HistoryNavigator<T> {

    final History<T> history;

    private int currentIndex = -1;
    private T current;

    public HistoryNavigator(History<T> history) {
        this.history = history;
    }

    public HistoryNavigator(Class<T> elementType, int maxItems, UnaryOperator<T> addOp) {
        this(new History<>(elementType, maxItems, addOp));
    }

    public void add(T element) {
        history.add(element);
        reset();
    }

    public T getPrevious(Supplier<T> currentSupplier) {
        if (history.getSize() == 0) {
            return null;
        } else if (currentIndex < 0 || currentIndex >= history.getSize() - 1) {
            if (this.current == null) {
                this.current = currentSupplier.get();
            }
            return history.getHistoryItem(currentIndex = 0);
        } else {
            if (this.current == null) {
                this.current = currentSupplier.get();
            }
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

    public T reset() {
        currentIndex = -1;
        final T current = this.current;
        this.current = null;
        return current;
    }

    public History<T> getHistory() {
        return history;
    }
}
