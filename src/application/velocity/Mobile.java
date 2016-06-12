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
import java.io.File;
import javafx.application.HostServices;
import javafx.scene.Scene;
import velocity.core.VelocityCore;
import velocity.manager.DownloadManager;
import velocity.manager.FavoritesManager;
import velocity.manager.HistoryManager;
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
        (new Thread(() -> {
            SettingsManager.initProperty("darkTheme", "false");
            SettingsManager.initProperty("downloadLocation", System.getProperty("user.home") + File.separator + "Downloads");
            SettingsManager.load();
            VelocityCore.setDefaultDownloadsLocation(SettingsManager.getProperty("downloadLocation"));
        })).start();
        (new Thread(() -> {
            DownloadManager.getInstance().load();
        })).start();
        (new Thread(() -> {
            FavoritesManager.getInstance().load();
        })).start();
        (new Thread(() -> {
            HistoryManager.getInstance().load();
        })).start();
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
    }

}
