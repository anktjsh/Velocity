/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.velocity;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.stage.Window;
import velocity.manager.SettingsManager;

/**
 *
 * @author Aniket
 */
public class Desktop extends Application {

    public static HostServices host;
    public static final String material = Desktop.class.getResource("material.css").toExternalForm();
    public static final Image web = new Image(Desktop.class.getResourceAsStream("web.png"));

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            e.printStackTrace();
            if (e instanceof Error) {
                Iterator<Window> impl = Stage.impl_getWindows();
                ArrayList<BrowserPane> bp = new ArrayList<>();
                while (impl.hasNext()) {
                    Window w = impl.next();
                    if (w instanceof Stage) {
                        Stage s = (Stage) w;
                        Parent root = s.getScene().getRoot();
                        if (root instanceof BrowserPane) {
                            bp.add((BrowserPane) root);
                        }
                    }
                }
                Platform.runLater(() -> {
                    for (BrowserPane b : bp) {
                        b.close();
                    }
                    Platform.exit();
                    System.exit(0);
                });
            }
        });
    }

    @Override
    public void start(Stage primary) {
        host = getHostServices();
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
        if (Boolean.parseBoolean(SettingsManager.getProperty("darkTheme"))) {
            primary.getScene().getStylesheets().add(material);
        }
        primary.getIcons().add(web);
        primary.setTitle("Velocity v" + BrowserPane.VERSION);
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
