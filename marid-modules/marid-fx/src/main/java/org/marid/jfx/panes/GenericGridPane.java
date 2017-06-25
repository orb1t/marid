package org.marid.jfx.panes;

import javafx.beans.value.WritableObjectValue;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.Double.MAX_VALUE;
import static javafx.geometry.HPos.LEFT;
import static javafx.scene.layout.Priority.*;
import static org.marid.jfx.LocalizedStrings.fls;

/**
 * @author Dmitry Ovchinnikov
 */
public class GenericGridPane extends GridPane {

    public GenericGridPane() {
        getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, NEVER, LEFT, false));
        getColumnConstraints().add(new ColumnConstraints(0, USE_COMPUTED_SIZE, MAX_VALUE, SOMETIMES, LEFT, true));
        setVgap(10);
        setHgap(10);
    }

    protected int getNextRowIndex() {
        return getChildren().stream().mapToInt(c -> c instanceof Separator ? 2 : 1).sum() / 2;
    }

    public TextField addTextField(String text, WritableObjectValue<String> value) {
        final TextField textField = new TextField(value.get());
        textField.textProperty().addListener((observable, oldValue, newValue) -> value.set(newValue));
        setHgrow(textField, ALWAYS);
        textField.setMaxWidth(Double.MAX_VALUE);
        final Label label = new Label();
        label.textProperty().bind(fls("%s: ", text));
        addRow(getNextRowIndex(), label, textField);
        return textField;
    }

    public CheckBox addBooleanField(String text, Supplier<Boolean> supplier, Consumer<Boolean> consumer) {
        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(supplier.get());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
        checkBox.setMaxWidth(Double.MAX_VALUE);
        setHgrow(checkBox, ALWAYS);
        final Label label = new Label();
        label.textProperty().bind(fls("%s: ", text));
        addRow(getNextRowIndex(), label, checkBox);
        return checkBox;
    }

    public Spinner<Integer> addIntField(String text, Supplier<Integer> supplier, Consumer<Integer> consumer, int low, int high, int step) {
        final Spinner<Integer> spinner = new Spinner<>(low, high, supplier.get(), step);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> consumer.accept(newValue));
        spinner.setMaxWidth(Double.MAX_VALUE);
        setHgrow(spinner, ALWAYS);
        final Label label = new Label();
        label.textProperty().bind(fls("%s: ", text));
        addRow(getNextRowIndex(), label, spinner);
        return spinner;
    }

    public <T extends Control> T addControl(String text, Supplier<T> nodeSupplier) {
        final Label label = new Label();
        label.textProperty().bind(fls("%s: ", text));
        final T node = nodeSupplier.get();
        node.setMaxWidth(Double.MAX_VALUE);
        setHgrow(node, ALWAYS);
        addRow(getNextRowIndex(), label, node);
        return node;
    }

    public Separator addSeparator() {
        final Separator separator = new Separator();
        separator.setPrefWidth(Double.MAX_VALUE);
        add(separator, 0, getNextRowIndex(), 2, 1);
        return separator;
    }
}
