package dev.jshfx.base.ui;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import dev.jshfx.fxmisc.richtext.CustomCodeArea;
import dev.jshfx.fxmisc.richtext.TextStyleSpans;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class ConsolePane extends EnvPane {

    private static final int OUTPUT_AREA_LIMIT = 1500;

    private CodeArea area = new CustomCodeArea();
    private Label header = new Label();
    private ContentPane contentPane;
    private ListChangeListener<? super TextStyleSpans> listener = (Change<? extends TextStyleSpans> c) -> {

        while (c.next()) {

            if (c.wasAdded()) {
                List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());

                Platform.runLater(() -> {
                    for (TextStyleSpans span : added) {
                        area.appendText(span.getText());
                        int from = area.getLength() - span.getStyleSpans().length();
                        area.setStyleSpans(from, span.getStyleSpans());
                    }

                    int paragraphCount = area.getParagraphs().size();
                    if (paragraphCount > OUTPUT_AREA_LIMIT) {
                        int lastExtraParagraph = paragraphCount - OUTPUT_AREA_LIMIT - 1;
                        area.deleteText(0, 0, lastExtraParagraph, area.getParagraph(lastExtraParagraph).length());
                    }

                    area.moveTo(area.getLength());
                    area.requestFollowCaret();
                });
            }
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
    public String getUserAgentStylesheet() {
        return getClass().getResource("console.css").toExternalForm();
    }

    public CodeArea getArea() {
        return area;
    }

    public void setContentPane(ContentPane contentPane) {

        area.clear();
        
        if (this.contentPane != null) {
            this.contentPane.getConsoleOutput().removeListener(listener);
        }

        this.contentPane = contentPane;

        if (contentPane != null) {
            header.textProperty().bind(contentPane.consoleHeaderTextProperty());
            contentPane.getConsoleOutput().addListener(listener);
            
            for (TextStyleSpans span : contentPane.getConsoleOutput()) {
                area.appendText(span.getText());
                int from = area.getLength() - span.getStyleSpans().length();
                area.setStyleSpans(from, span.getStyleSpans());
            }
            
            area.moveTo(area.getLength());
            area.requestFollowCaret();
        }
    }

    public void dispose() {
        area.dispose();
    }
}
