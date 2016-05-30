/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.velocity;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.Theme;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import velocity.core.VelocityCore;
import velocity.manager.SettingsManager;

/**
 *
 * @author Aniket
 */
public class Mobile extends MobileApplication {

    public static HostServices host;

    @Override
    public void init() {
        host = getHostServices();
        addViewFactory(HOME_VIEW, () -> {
            View v = new View("MAIN") {
                @Override
                protected void updateAppBar(AppBar appBar) {
                    appBar.setVisible(false);
                }

            };
            v.setCenter(new BrowserPane());
            return v;
        });
    }

    @Override
    public void postInit(Scene scene) {
        if (Boolean.parseBoolean(SettingsManager.getProperty("darkTheme"))) {
            Theme.DARK.assignTo(scene);
        }
        if (VelocityCore.isDesktop()) {
            Stage primary = (Stage) scene.getWindow();
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
                            ((BrowserPane) retrieveView(HOME_VIEW).get().getCenter()).loadFiles(db.getFiles());
                        } else if (db.getContent(new DataFormat("Tab")) != null) {

                        }
                        event.setDropCompleted(success);
                        event.consume();
                    });
            primary.getScene().setOnKeyPressed((e) -> {
                ((BrowserPane) retrieveView(HOME_VIEW).get().getCenter()).resolve(e);
            });
            primary.getIcons().add(Desktop.web);
            primary.setTitle("Velocity v" + BrowserPane.VERSION);
            primary.setOnHidden((e) -> {
                Desktop.close(((BrowserPane) retrieveView(HOME_VIEW).get().getCenter()));
            });
        }
    }

}
