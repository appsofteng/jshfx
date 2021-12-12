package dev.jshfx.fxmisc.richtext;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.EventPattern.mousePressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.fxmisc.wellbehaved.event.Nodes;

import dev.jshfx.jfx.scene.layout.LayoutUtils;
import dev.jshfx.jfx.util.FXResourceBundle;
import dev.jshfx.jx.tools.JavaSourceResolver.HtmlDoc;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;

public class CompletionPopup extends Tooltip {

    private static CompletionPopup INSTANCE;
    static final double DEFAULT_WIDTH = 450;
    static final double DEFAULT_HEIGHT = 200;
    private ListView<CompletionItem> itemView = new ListView<>();
    private Label placeHolder = new Label();
    private DocPopup docPopup;
    private ChangeListener<Boolean> focusListener;
    private ChangeListener<Number> windowListener;
    private List<CompletionItem> buffer = new ArrayList<>();

    private EventHandler<KeyEvent> keyHandler;
    private EventHandler<MouseEvent> mouseHandler;

    private CompletionPopup() {
        docPopup = new DocPopup();
        // does not work well because it blocks mouse press events outside the popup
        // setAutoHide(true);

        // not working when the list inside the popup has the focus
        setHideOnEscape(false);

        setMinSize(10, 10);
        setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        itemView.setPlaceholder(placeHolder);
        StackPane pane = new StackPane(itemView);
        pane.setPadding(new Insets(5));
        setGraphic(pane);
        LayoutUtils.makeResizable(this, pane, 5);

        setBehavior();
    }

    public static CompletionPopup get() {
        if (INSTANCE == null) {
            INSTANCE = new CompletionPopup();
        }

        return INSTANCE;
    }

    public void setDocumentation(Function<CompletionItem, HtmlDoc> documentation) {
        docPopup.setDocumentation(documentation);
    }

    public void setCompletionItem(BiFunction<String, HtmlDoc, CompletionItem> completionItem) {
        docPopup.setCompletionItem(completionItem);
    }

    public void clear() {
        buffer.clear();
        itemView.getItems().clear();
    }

    public boolean add(CompletionItem item) {

        if (isShowing()) {
            if (item != null) {
                buffer.add(item);
            }

            if (buffer.size() > 4 || item == null) {
                var copy = new ArrayList<>(buffer);
                Platform.runLater(() -> {
                    itemView.getItems().addAll(copy);
                    if (itemView.getSelectionModel().isEmpty()) {
                        getGraphic().requestFocus();
                        itemView.getSelectionModel().clearSelection();
                        itemView.getSelectionModel().selectFirst();
                    }
                });
                buffer.clear();
            }
            
            if (item == null && itemView.getItems().isEmpty()) {
                Platform.runLater(() -> placeHolder.setText(FXResourceBundle.getBundle().getString​("nothingFound")));
            }
        } 
        
        return isShowing();
    }

    public void setItems(Collection<? extends CompletionItem> items) {
        itemView.setItems(FXCollections.observableArrayList(items));
    }

    private void setBehavior() {

        focusListener = (v, o, n) -> {

            if (!n) {
                v.removeListener(focusListener);
                close();
            }
        };

        windowListener = (v, o, n) -> {
            v.removeListener(windowListener);
            close();
        };

        keyHandler = e -> {

            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                selected();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                close();
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                selectPrevious();
                e.consume();
            } else if (e.getCode() == KeyCode.DOWN) {
                selectNext();
                e.consume();
            }
        };

        mouseHandler = e -> {
            close();
        };

        Nodes.addInputMap(itemView,
                sequence(consume(keyPressed(ENTER), e -> selected()), consume(keyPressed(ESCAPE), e -> close()),
                        consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 2), e -> selected())));
        
        MenuItem item = new MenuItem();
        FXResourceBundle.getBundle().put(item.textProperty(), "importStatic");
        item.setOnAction(e -> selectedStatic());
        
        ContextMenu menu = new ContextMenu(item);
        menu.setOnShowing(e -> item.setDisable(!itemView.getSelectionModel().getSelectedItem().isStatic()));
        itemView.setContextMenu(menu);

        anchorXProperty().addListener((v, o, n) -> {
            double offset = Screen.getPrimary().getBounds().getWidth() - n.doubleValue() - getPrefWidth() > n
                    .doubleValue() ? getPrefWidth() : -docPopup.getPrefWidth();
            docPopup.setAnchorX(n.doubleValue() + offset);
        });

        anchorYProperty().addListener((v, o, n) -> {
            docPopup.setAnchorY(n.doubleValue());
        });

        itemView.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {

            if (!isShowing()) {
                return;
            }

            if (n != null) {

                if (docPopup.loadContent(n)) {
                    if (!docPopup.isShowing()) {
                        docPopup.show(this);
                        docPopup.getGraphic().requestFocus();
                    }
                } else {
                    docPopup.hide();
                }
            } else {
                docPopup.hide();
            }
        });
    }

    private void selectPrevious() {

        if (itemView.getSelectionModel().getSelectedIndex() == 0) {
            itemView.getSelectionModel().select(itemView.getItems().size() - 1);
        } else {
            itemView.getSelectionModel().selectPrevious();
        }

        itemView.scrollTo(itemView.getSelectionModel().getSelectedIndex());
    }

    private void selectNext() {
        if (itemView.getSelectionModel().getSelectedIndex() == itemView.getItems().size() - 1) {
            itemView.getSelectionModel().select(0);
        } else {
            itemView.getSelectionModel().selectNext();
        }

        itemView.scrollTo(itemView.getSelectionModel().getSelectedIndex());
    }

    public CompletionItem getSelection() {
        return itemView.getSelectionModel().getSelectedItem();
    }

    public void close() {
        hide();
    }

    private void selected() {
        hide();
        CompletionItem selection = itemView.getSelectionModel().getSelectedItem();
        if (selection != null) {
            selection.complete();
        }
    }
    
    private void selectedStatic() {
        hide();
        CompletionItem selection = itemView.getSelectionModel().getSelectedItem();
        if (selection != null) {
            selection.completeStatic();
        }
    }

    @Override
    public void hide() {
        if (isShowing()) {
            getOwnerNode().removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            getOwnerNode().removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseHandler);
            docPopup.hide();
            super.hide();
        }
    }

    @Override
    public void show(Node ownerNode, double anchorX, double anchorY) {
        placeHolder.setText(FXResourceBundle.getBundle().getString​("searching"));
        if (isShowing()) {
            setAnchorX(anchorX);
            setAnchorY(anchorY);
            itemView.getSelectionModel().clearSelection();
            itemView.getSelectionModel().selectFirst();
        } else {
            ownerNode.focusedProperty().addListener(focusListener);
            ownerNode.getScene().getWindow().xProperty().addListener(windowListener);
            ownerNode.getScene().getWindow().yProperty().addListener(windowListener);
            ownerNode.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            ownerNode.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseHandler);

            super.show(ownerNode, anchorX, anchorY);

            getGraphic().requestFocus();
            itemView.getSelectionModel().clearSelection();
            itemView.getSelectionModel().selectFirst();
        }
    }
}
