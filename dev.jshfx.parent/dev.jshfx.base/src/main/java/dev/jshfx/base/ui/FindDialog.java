package dev.jshfx.base.ui;

import java.util.regex.Pattern;

import dev.jshfx.fonts.Fonts;
import dev.jshfx.jfx.scene.control.AutoCompleteField;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Window;

public class FindDialog extends Dialog<Void> {

    private ReadOnlyObjectProperty<ContentPane> contentPane;

    private AutoCompleteField<String> findField = new AutoCompleteField<>();
    private AutoCompleteField<String> replaceField = new AutoCompleteField<>();

    private Button findPreviousButton;
    private Button findNextButton;

    private CheckBox matchCaseCheck = new CheckBox();
    private CheckBox wholeWordCheck = new CheckBox();
    private CheckBox regexCheck = new CheckBox();
    private CheckBox inSelectionCheck = new CheckBox();

    private Button replacePreviousButton;
    private Button replaceNextButton;
    private Button replaceAllButton = new Button();

    public FindDialog(Window window, ReadOnlyObjectProperty<ContentPane> contentPane) {
        this.contentPane = contentPane;
        initOwner(window);
        initModality(Modality.NONE);
        setTitle(FXResourceBundle.getBundle().getString​("find"));

        setSelection();

        FXResourceBundle.getBundle().put(findField.promptTextProperty(), "findText");
        FXResourceBundle.getBundle().put(replaceField.promptTextProperty(), "replaceText");

        findPreviousButton = new Button(Fonts.FontAwesome.CHEVRON_UP);
        findPreviousButton.setFont(Font.font(Fonts.FONT_AWESOME_5_FREE_SOLID));
        findPreviousButton.setFocusTraversable(false);
        findPreviousButton.setMaxHeight(Double.MAX_VALUE);
        findPreviousButton.setMinWidth(Region.USE_PREF_SIZE);
        findPreviousButton.disableProperty().bind(findField.textProperty().isEmpty());
        findPreviousButton.setOnAction(e -> contentPane.get().getFinder().findPrevious(getPattern(), inSelectionCheck.isSelected()));

        findNextButton = new Button(Fonts.FontAwesome.CHEVRON_DOWN);
        findNextButton.setFont(Font.font(Fonts.FONT_AWESOME_5_FREE_SOLID));
        findNextButton.setFocusTraversable(false);
        findNextButton.setMaxHeight(Double.MAX_VALUE);
        findNextButton.setMinWidth(Region.USE_PREF_SIZE);
        findNextButton.disableProperty().bind(findField.textProperty().isEmpty());
        findNextButton.setOnAction(e -> contentPane.get().getFinder().findNext(getPattern(), inSelectionCheck.isSelected()));

        HBox.setHgrow(findField, Priority.ALWAYS);
        HBox fieldBox = new HBox(findField, findPreviousButton, findNextButton);

        FXResourceBundle.getBundle().put(matchCaseCheck.textProperty(), "matchCase");
        FXResourceBundle.getBundle().put(wholeWordCheck.textProperty(), "wholeWord");
        FXResourceBundle.getBundle().put(regexCheck.textProperty(), "regex");
        FXResourceBundle.getBundle().put(inSelectionCheck.textProperty(), "inSelection");

        wholeWordCheck.disableProperty().bind(regexCheck.selectedProperty());
        regexCheck.disableProperty().bind(wholeWordCheck.selectedProperty());

        HBox optionBox = new HBox(5, matchCaseCheck, wholeWordCheck, regexCheck, inSelectionCheck);

        replacePreviousButton = new Button(Fonts.FontAwesome.CHEVRON_UP);
        replacePreviousButton.setFont(Font.font(Fonts.FONT_AWESOME_5_FREE_SOLID));
        replacePreviousButton.setFocusTraversable(false);
        replacePreviousButton.setMaxHeight(Double.MAX_VALUE);
        replacePreviousButton.setMinWidth(Region.USE_PREF_SIZE);
        replacePreviousButton.disableProperty().bind(findField.textProperty().isEmpty());
        replacePreviousButton.setOnAction(e -> contentPane.get().getFinder().replacePrevious(getPattern(), inSelectionCheck.isSelected()));

        replaceNextButton = new Button(Fonts.FontAwesome.CHEVRON_DOWN);
        replaceNextButton.setFont(Font.font(Fonts.FONT_AWESOME_5_FREE_SOLID));
        replaceNextButton.setFocusTraversable(false);
        replaceNextButton.setMaxHeight(Double.MAX_VALUE);
        replaceNextButton.setMinWidth(Region.USE_PREF_SIZE);
        replaceNextButton.disableProperty().bind(findField.textProperty().isEmpty());
        replaceNextButton.setOnAction(e -> contentPane.get().getFinder().replaceNext(getPattern(), inSelectionCheck.isSelected()));
        
        HBox.setHgrow(replaceField, Priority.ALWAYS);
        HBox replaceFieldBox = new HBox(replaceField, replacePreviousButton, replaceNextButton);
        
        FXResourceBundle.getBundle().put(replaceAllButton.textProperty(), "replaceAll");

        replaceAllButton.disableProperty().bind(findField.textProperty().isEmpty());
        replaceAllButton.setOnAction(e -> contentPane.get().getFinder().replaceAll(getPattern(), inSelectionCheck.isSelected()));

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(replaceAllButton);

        VBox vbox = new VBox(10, fieldBox, replaceFieldBox, optionBox, buttonBar);

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        getDialogPane().setContent(vbox);
        findField.requestFocus();
    }

    public void setSelection() {
        findField.setText(contentPane.get().getSelection());
    }

    private Pattern getPattern() {

        String regex = findField.getText();

        int flags = matchCaseCheck.isSelected() ? 0 : Pattern.CASE_INSENSITIVE;

        if (wholeWordCheck.isSelected()) {
            regex = String.format("\\b%s\\b", regex);
        } else if (regexCheck.isDisabled() || !regexCheck.isSelected()) {
            flags |= Pattern.LITERAL;
        }

        Pattern pattern = Pattern.compile(regex, flags);

        return pattern;
    }
}
