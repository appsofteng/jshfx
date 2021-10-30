package dev.jshfx.base.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.input.Clipboard;
import javafx.util.Duration;

public class BindingManager {

    private ScheduledService<Void> clipboardService;

    private ReadOnlyBooleanWrapper allSelected = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper clear = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper clipboardEmpty = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper selectionEmpty = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper redoEmpty = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper undoEmpty = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper historyStartReached = new ReadOnlyBooleanWrapper();
    private ReadOnlyBooleanWrapper historyEndReached = new ReadOnlyBooleanWrapper();

    public BindingManager(RootPane rootPane) {

        clipboardService = new ScheduledService<>() {
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() {

                        Platform.runLater(() -> clipboardEmpty.set(!Clipboard.getSystemClipboard().hasString()));

                        return null;
                    }
                };
            }
        };
        clipboardService.setPeriod(Duration.seconds(1));

        bind(rootPane);
    }

    private void bind(RootPane rootPane) {

        rootPane.selectedShellProperty().addListener((v, o, n) -> {
            if (n != null) {

                var inputArea = n.getConsolePane().getInputArea();
                var outputArea = n.getConsolePane().getOutputArea();

                allSelected.bind(Bindings.createBooleanBinding(
                        () -> n.getConsolePane().getFocusedArea() != null && n.getConsolePane().getFocusedArea()
                                .getSelectedText().length() == n.getConsolePane().getFocusedArea().getText().length(),
                                        n.getConsolePane().focusedAreaProperty(), inputArea.selectedTextProperty(),
                        outputArea.selectedTextProperty()));

                selectionEmpty.bind(Bindings.createBooleanBinding(
                        () -> n.getConsolePane().getFocusedArea() != null
                                && n.getConsolePane().getFocusedArea().getSelection().getLength() == 0,
                                        n.getConsolePane().focusedAreaProperty(), inputArea.selectionProperty(),
                        outputArea.selectionProperty()));

                clear.bind(Bindings.createBooleanBinding(
                        () -> n.getConsolePane().getFocusedArea() != null
                                && n.getConsolePane().getFocusedArea().getLength() == 0,
                                        n.getConsolePane().focusedAreaProperty(), inputArea.lengthProperty(),
                        outputArea.lengthProperty()));

                redoEmpty.bind(Bindings.createBooleanBinding(() -> !inputArea.isRedoAvailable(),
                        inputArea.redoAvailableProperty()));
                undoEmpty.bind(Bindings.createBooleanBinding(() -> !inputArea.isUndoAvailable(),
                        inputArea.redoAvailableProperty()));

                historyStartReached.bind(n.getConsolePane().historyStartReachedProperty());
                historyEndReached.bind(n.getConsolePane().historyEndReachedProperty());

            } else {
                allSelected.unbind();
                clear.unbind();
                selectionEmpty.unbind();
                redoEmpty.unbind();
                undoEmpty.unbind();
                historyStartReached.unbind();
                historyEndReached.unbind();
            }
        });
    }

    public void cancel() {
        clipboardService.cancel();
    }

    public ReadOnlyBooleanProperty allSelectedProperty() {
        return allSelected.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty clearProperty() {
        return clear.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty clipboardEmptyProperty() {
        return clipboardEmpty.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty selectionEmptyProperty() {
        return selectionEmpty.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty redoEmptyProperty() {
        return redoEmpty.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty undoEmptyProperty() {
        return undoEmpty.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty historyStartReachedProperty() {
        return historyStartReached.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty historyEndReachedProperty() {
        return historyEndReached.getReadOnlyProperty();
    }
}
