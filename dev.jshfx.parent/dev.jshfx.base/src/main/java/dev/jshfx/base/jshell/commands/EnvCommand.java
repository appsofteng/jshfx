package dev.jshfx.base.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jshfx.base.jshell.CommandProcessor;
import dev.jshfx.base.jshell.Env;
import dev.jshfx.base.jshell.EnvBox;
import dev.jshfx.base.sys.FileManager;
import dev.jshfx.j.util.json.JsonUtils;
import dev.jshfx.jfx.util.FXResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/env")
public class EnvCommand extends BaseCommand {

    @Option(names = "-gui", descriptionKey = "/env.-gui")
    private boolean gui;

    public EnvCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (gui) {
            Platform.runLater(() -> {

                Dialog<Void> dialog = new Dialog<>();
                dialog.initOwner(commandProcessor.getSession().getWindow());
                dialog.setTitle(FXResourceBundle.getBundle().getString​("environment"));
                EnvBox envBox = new EnvBox(getEnvs());
                DialogPane dialogPane = dialog.getDialogPane();
                dialogPane.setContent(envBox);
                ButtonType resetdButtonType = new ButtonType(FXResourceBundle.getBundle().getString​("reset"),
                        ButtonData.OK_DONE);
                ButtonType reloadButtonType = new ButtonType(FXResourceBundle.getBundle().getString​("reload"),
                        ButtonData.APPLY);
                ButtonType cancelButtonType = new ButtonType(FXResourceBundle.getBundle().getString​("cancel"),
                        ButtonData.CANCEL_CLOSE);
                dialogPane.getButtonTypes().addAll(resetdButtonType, reloadButtonType, cancelButtonType);
                final Button reset = (Button) dialogPane.lookupButton(resetdButtonType);
                reset.setOnAction(e -> {
                    dialog.close();
                    resetEnvs(envBox.getEnv(), envBox.getEnvs());
                });
                final Button reload = (Button) dialogPane.lookupButton(reloadButtonType);
                reload.setOnAction(e -> {
                    dialog.close();
                    reloadEnvs(envBox.getEnv(), envBox.getEnvs());
                });
                final Button cancel = (Button) dialogPane.lookupButton(cancelButtonType);
                cancel.setOnAction(e -> dialog.close());

                dialog.showAndWait();

            });
        }
    }

    private ObservableList<Env> getEnvs() {
        ObservableList<Env> envs = FXCollections.observableArrayList();
        envs.add(commandProcessor.getSession().loadEnv());

        List<Env> envsList = JsonUtils.get().fromJson(FileManager.ENVS_FILE, new ArrayList<Env>() {
        }.getClass().getGenericSuperclass(), new ArrayList<>());
        envs.addAll(envsList);

        return envs;
    }

    private void resetEnvs(Env env, ObservableList<Env> envs) {

        commandProcessor.getSession().resetEnv(env);
        saveEnvs(env, envs);

    }

    private void reloadEnvs(Env env, ObservableList<Env> envs) {
        commandProcessor.getSession().reloadEnv(env);
        saveEnvs(env, envs);
    }

    private void saveEnvs(Env env, ObservableList<Env> envs) {
        envs.remove(env);
        JsonUtils.get().toJson(envs, FileManager.ENVS_FILE);
    }
}
