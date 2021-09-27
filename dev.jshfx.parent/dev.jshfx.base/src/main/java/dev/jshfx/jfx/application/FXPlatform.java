package dev.jshfx.jfx.application;

import javafx.application.Platform;

public final class FXPlatform {

    private FXPlatform() {
    }

    public static void runFX(Runnable run) {
        if (Platform.isFxApplicationThread()) {
           run.run();
        } else {
            Platform.runLater(run);
        }
    }
}
