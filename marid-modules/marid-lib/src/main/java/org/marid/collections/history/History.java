package org.marid.collections.history;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

import static org.marid.util.Utils.cast;

/**
 * @author Dmitry Ovchinnikov
 */
public class History<T> {

    private final Class<T> elementType;
    private final int maxItems;
    private final List<T> history = new ArrayList<>();
    private final UnaryOperator<T> addOp;

    public History(Class<T> elementType, int maxItems, UnaryOperator<T> addOp) {
        this.elementType = elementType;
        this.maxItems = maxItems;
        this.addOp = addOp;
    }

    public void add(T element) {
        final T value = addOp.apply(element);
        if (value != null) {
            history.remove(value);
            history.add(0, value);
        }
    }

    public void load(Preferences node) {
        final byte[] data = node.getByteArray("history", null);
        if (data != null) {
            try (final ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(data))) {
                final T[] array = cast(is.readObject());
                history.clear();
                for (int i = 0; i < array.length && history.size() < maxItems; i++) {
                    history.add(array[i]);
                }
            } catch (IOException | ClassNotFoundException x) {
                throw new IllegalStateException(x);
            }
        }
    }

    public void save(Preferences node) {
        if (history.isEmpty()) {
            node.remove("history");
        } else {
            final T[] elements = cast(history.toArray((Object[]) (Array.newInstance(elementType, history.size()))));
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(elements);
            } catch (IOException x) {
                throw new IllegalStateException(x);
            }
            node.putByteArray("history", bos.toByteArray());
        }
    }

    public T getHistoryItem(int index) {
        return history.get(index);
    }

    public int getSize() {
        return history.size();
    }

    public boolean containsItem(T item) {
        return history.contains(item);
    }

    public UnaryOperator<T> getAddOp() {
        return addOp;
    }
}
