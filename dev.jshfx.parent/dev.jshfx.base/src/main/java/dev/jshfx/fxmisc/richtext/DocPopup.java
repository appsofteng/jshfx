package dev.jshfx.fxmisc.richtext;

import static javafx.scene.input.KeyCode.ESCAPE;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import org.fxmisc.wellbehaved.event.Nodes;

import dev.jshfx.jfx.application.FXPlatform;
import dev.jshfx.jfx.scene.layout.LayoutUtils;
import dev.jshfx.jfx.scene.web.JSUtils;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class DocPopup extends Tooltip {

    private WebView webView;
    private Function<CompletionItem, String> documentation;
    private Function<String, CompletionItem> completionItem;
    private ContextMenu contextMenu;
    private ObservableList<CompletionItem> history = FXCollections.observableArrayList();
    private IntegerProperty historyIndex = new SimpleIntegerProperty(-1);

    public DocPopup() {
        setMinSize(10, 10);
        setPrefSize(CompletionPopup.DEFAULT_WIDTH, CompletionPopup.DEFAULT_HEIGHT);

        FXPlatform.runFX(() -> {
            webView = new WebView();
            webView.setContextMenuEnabled(false);
            StackPane pane = new StackPane(webView);
            pane.setPadding(new Insets(5));
            setGraphic(pane);
            LayoutUtils.makeResizable(this, pane, 5);
            createContextMenu();
            setBehavior();
        });
    }

    public void setDocumentation(Function<CompletionItem, String> documentation) {
        this.documentation = documentation;
    }

    public void setCompletionItem(Function<String, CompletionItem> completionItem) {
        this.completionItem = completionItem;
    }

    private void setBehavior() {
        setHideOnEscape(false);
        Nodes.addInputMap(webView, sequence(consume(keyPressed(ESCAPE), e -> getOwnerWindow().hide())));

        webView.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {

            if (e.getButton() == MouseButton.PRIMARY) {
                String url = JSUtils.getLinkUrl(webView.getEngine(), e.getX(), e.getY());
                if (url != null) {

                    if (url.matches("https?://.*")) {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            try {
                                Desktop.getDesktop().browse(new URI(url));
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            } 
                        }
                    } else {
                        history.remove(getHistoryIndex() + 1, history.size());
                        loadContent(completionItem.apply(url));
                    }
                }
            }

            if (e.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(webView.getScene().getWindow(), e.getScreenX(), e.getScreenY());
            }
        });
    }

    private void createContextMenu() {
        contextMenu = new ContextMenu();
        contextMenu.setAutoHide(true);

        MenuItem back = new MenuItem();
        back.textProperty().bind(FXResourceBundle.getBundle().getStringBinding("back"));
        back.disableProperty().bind(historyIndex.lessThanOrEqualTo(0));
        back.setOnAction(e -> back());

        MenuItem forward = new MenuItem();
        forward.textProperty().bind(FXResourceBundle.getBundle().getStringBinding("forward"));
        forward.disableProperty()
                .bind(Bindings.isEmpty(history).or(historyIndex.isEqualTo(Bindings.size(history).subtract(1))));
        forward.setOnAction(e -> forward());

        contextMenu.getItems().addAll(back, forward);
    }

    boolean loadContent(CompletionItem docRef) {

        String doc = documentation.apply(docRef);
        if (!doc.isEmpty()) {
            history.add(docRef);
            moveHistory(1);
            webView.getEngine().getLoadWorker().cancel();
            webView.getEngine().load("");
            webView.getEngine().loadContent(doc);
        }

        return !doc.isBlank();
    }

    private void load(CompletionItem docRef) {
        String doc = documentation.apply(docRef);
        if (!doc.isEmpty()) {
            webView.getEngine().getLoadWorker().cancel();
            webView.getEngine().load("");
            webView.getEngine().loadContent(doc);
        }
    }

    private int getHistoryIndex() {
        return historyIndex.get();
    }

    private void moveHistory(int value) {
        historyIndex.set(historyIndex.get() + value);
    }

    private void back() {
        if (getHistoryIndex() > 0) {
            moveHistory(-1);
            loadHistory();
        }
    }

    private void forward() {
        if (getHistoryIndex() + 1 < history.size()) {
            moveHistory(1);
            loadHistory();
        }
    }

    private void loadHistory() {
        CompletionItem docRef = history.get(getHistoryIndex());
        load(docRef);
    }
}
