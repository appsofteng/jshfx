package dev.jshfx.base.ui;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.AreaWrapper;
import dev.jshfx.fxmisc.richtext.CustomCodeArea;
import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class ConsolePane extends EnvPane {

    private CodeArea area = new CustomCodeArea();
    private AreaWrapper<CodeArea> areaWrapper = new AreaWrapper<>(area);
    private Label header = new Label();
    private ContentPane contentPane;
    private Finder finder = new FinderImpl(area);;
    private ChangeListener<? super TextStyleSpans> listener = (v, o, n) -> {

        if (n != null) {
            String text = n.getText();
            var styleSpans = n.getStyleSpans();

            Platform.runLater(() -> {
                area.replaceText(text);
                area.setStyleSpans(0, styleSpans);

                area.moveTo(area.getLength());
                area.requestFollowCaret();
            });
        }
    };

    public ConsolePane() {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(header);
        area.setEditable(false);
        area.setFocusTraversable(false);
        borderPane.setCenter(new VirtualizedScrollPane<>(area));

        getChildren().add(borderPane);

        // The style must be added explicitly.
        getStylesheets().add(getUserAgentStylesheet());
    }

    @Override
    public void setActions(Actions actions) {
        super.setActions(actions);

        actions.setReadOnlyContextMenu(getArea());
        actions.addConsoleKeyHandlers(getArea());

        handlers.put(actions.getCopyAction(), () -> area.copy());
        handlers.put(actions.getCutAction(), () -> area.cut());
        handlers.put(actions.getSelectAllAction(), () -> area.selectAll());
        handlers.put(actions.getClearAction(), () -> clear());
    }

    @Override
    public void bindActions(Actions actions) {
        super.bindActions(actions);

        actions.getSelectAllAction().disabledProperty().bind(areaWrapper.allSelectedProperty());
        actions.getCopyAction().disabledProperty().bind(areaWrapper.selectionEmptyProperty());
        actions.getClearAction().disabledProperty().bind(areaWrapper.clearProperty());
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("console.css").toExternalForm();
    }

    public CodeArea getArea() {
        return area;
    }

    @Override
    public Finder getFinder() {
        return finder;
    }

    public void setContentPane(ContentPane contentPane) {

        area.clear();
        header.textProperty().unbind();
        header.setText("");

        if (this.contentPane != null) {
            this.contentPane.getConsoleModel().outputProperty().removeListener(listener);
        }

        this.contentPane = contentPane;

        if (contentPane != null) {
            header.textProperty().bind(contentPane.consoleHeaderTextProperty());
            contentPane.getConsoleModel().outputProperty().addListener(listener);

            area.replaceText(contentPane.getConsoleModel().getOutput().getText());
            area.setStyleSpans(0, contentPane.getConsoleModel().getOutput().getStyleSpans());
            
            area.moveTo(area.getLength());
            area.requestFollowCaret();
        }
    }

    public void dispose() {
        area.dispose();
    }

    private void clear() {
        area.clear();

        if (contentPane != null) {
            contentPane.getConsoleModel().clear();
        }
    }
}
