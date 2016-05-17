/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import velocity.cookie.CookieManager;
import java.net.CookieHandler;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

/**
 *
 * @author Aniket
 */
public class Browser extends Application {

    public static HostServices host;

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            ErrorConsole.addError(t, e, "");
            e.printStackTrace();
        });
    }

    @Override
    public void start(Stage primary) {
        host = getHostServices();
        ErrorConsole.initialize(primary);
        BrowserPane pane;
        primary.setScene(new Scene(pane = new BrowserPane()));
        primary.getScene()
                .setOnDragOver((event) -> {
                    Dragboard db = event.getDragboard();
                    if (db.hasFiles()) {
                        event.acceptTransferModes(TransferMode.COPY);
                    } else {
                        event.consume();
                    }
                });
        primary.getScene()
                .setOnDragDropped((event) -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasFiles()) {
                        success = true;
                        pane.loadFiles(db.getFiles());
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });
        primary.getScene().setOnKeyPressed((e) -> {
            pane.resolve(e);
        });
        primary.getScene().getStylesheets().add(getClass().getResource("material.css").toExternalForm());
        primary.getIcons().add(new Image(getClass().getResourceAsStream("web.png")));
        primary.setTitle("Velocity v1.0.0");
        primary.setOnHidden((e) -> {
            close(pane);
        });
        primary.show();
    }

    public static void close(BrowserPane pane) {
        pane.close();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String args[]) {
        launch(args);
    }
}
