package dev.jshfx.base.jshell.commands;

import dev.jshfx.base.ui.ConsoleModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public abstract class Command {

    private final String name;
    protected final JShell jshell;
    protected final ConsoleModel consoleModel;
    protected final ObservableList<String> history;


    public Command(String name, JShell jshell, ConsoleModel consoleModel) {
        this(name, jshell, consoleModel, FXCollections.emptyObservableList());
    }

    public Command(String name, JShell jshell, ConsoleModel consoleModel, ObservableList<String> history) {
        this.name = name;
        this.jshell = jshell;
        this.consoleModel = consoleModel;
        this.history = history;
    }

    public boolean matches(String input) {
        return input.startsWith(name);
    }

    public String getName() {
        return name;
    }

    public abstract void execute(String input);

    @Override
    public String toString() {
        return name;
    }

}
