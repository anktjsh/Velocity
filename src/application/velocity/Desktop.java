/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.velocity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import velocity.core.Download;
import velocity.core.VelocityCore;
import velocity.handler.DownloadResult;
import velocity.manager.DownloadManager;
import velocity.manager.FavoritesManager;
import velocity.manager.HistoryManager;
import velocity.manager.SettingsManager;
import velocity.util.DialogUtils;

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
        (new Thread(new AutoUpdater())).start();
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

    public static Stage getBrowser(Stage primary) {
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
        return primary;
    }

    public static Stage getBrowser() {
        Stage primary = new Stage();
        return getBrowser(primary);
    }

    @Override
    public void start(Stage primary) {
        host = getHostServices();
        getBrowser(primary).show();
    }

    public static void close(BrowserPane pane) {
        pane.close();
        if (!Window.impl_getWindows().hasNext()) {
            Platform.exit();
            System.exit(0);
        }
    }

    public static void main(String args[]) {
        launch(args);
    }

    private class AutoUpdater implements Runnable {

        @Override
        public void run() {
            if (VelocityCore.isDesktop()) {
                String OS = System.getProperty("os.name").toLowerCase();
                String ver = increment();
                if (!ver.equals(BrowserPane.VERSION)) {
                    if (ver.compareTo(BrowserPane.VERSION) > 1) {
                        String se;
                        if (OS.startsWith("win")) {
                            se = "https://github.com/anktjsh/Velocity/releases/download/v" + ver + "/Velocity-" + ver + ".exe";
                        } else {
                            se = "https://github.com/anktjsh/Velocity/releases/download/v" + ver + "/Velocity-" + ver + ".pkg";
                        }
                        File f = new File(new File("").getAbsolutePath() + File.separator + "Velocity-" + ver + "." + (OS.startsWith("win") ? "exe" : "pkg"));
                        Download d = new Download(se, f, null, DownloadResult.CUSTOM);
                        d.setOnSucceeded((e) -> {
                            Optional<ButtonType> o = DialogUtils.showAlert(AlertType.CONFIRMATION, null, "Update", "An Update to Velocity to available!\nWould you like to restart Velocity?", "");
                            if (o.isPresent()) {
                                if (o.get() == ButtonType.OK) {
                                    (new Thread(() -> {
                                        update(d.getLocalFile());
                                        Iterator<Window> w = Window.impl_getWindows();
                                        while (w.hasNext()) {
                                            Window ww = w.next();
                                            if (ww instanceof Stage) {
                                                Stage s = (Stage) ww;
                                                if (s.getScene().getRoot() instanceof BrowserPane) {
                                                    s.fireEvent(
                                                            new WindowEvent(
                                                                    s,
                                                                    WindowEvent.WINDOW_CLOSE_REQUEST
                                                            )
                                                    );
                                                }
                                            }
                                        }
                                    })).start();
                                }
                            }
                        });
                        (new Thread(d)).start();
                    }
                }
            }
        }

        public void update(File loc) {
            if (loc != null) {
                ProcessBuilder pb = new ProcessBuilder(loc.getAbsolutePath());
                pb.redirectErrorStream(true);
                try {
                    Process p = pb.start();
                } catch (IOException ex) {
                }
            }
        }

        private String increment() {
            String[] spl = BrowserPane.VERSION.split("\\.");
            spl[0] = (Integer.parseInt(spl[0]) + 1) + "";
            spl[1] = "0";
            spl[2] = "0";
            while (true) {
                String combo = spl[0] + "." + spl[1] + "." + spl[2];
                if (!exists(combo)) {
                    spl[0] = (Integer.parseInt(spl[0]) - 1) + "";
                    break;
                } else {
                    spl[0] = (Integer.parseInt(spl[0]) + 1) + "";
                }
            }
            spl[1] = (Integer.parseInt(spl[1]) + 1) + "";
            spl[2] = "0";
            while (true) {
                String combo = spl[0] + "." + spl[1] + "." + spl[2];
                if (!exists(combo)) {
                    spl[1] = (Integer.parseInt(spl[1]) - 1) + "";
                    break;
                } else {
                    spl[1] = (Integer.parseInt(spl[1]) + 1) + "";
                }
            }
            spl[2] = (Integer.parseInt(spl[2]) + 1) + "";
            while (true) {
                String combo = spl[0] + "." + spl[1] + "." + spl[2];
                if (!exists(combo)) {
                    spl[2] = (Integer.parseInt(spl[2]) - 1) + "";
                    break;
                } else {
                    spl[2] = (Integer.parseInt(spl[2]) + 1) + "";
                }
            }
            return spl[0] + "." + spl[1] + "." + spl[2];
        }

        private boolean exists(String ver) {
            String OS = System.getProperty("os.name").toLowerCase();
            String se;
            if (OS.startsWith("win")) {
                se = "https://github.com/anktjsh/Velocity/releases/download/v" + ver + "/Velocity-" + ver + ".exe";
            } else {
                se = "https://github.com/anktjsh/Velocity/releases/download/v" + ver + "/Velocity-" + ver + ".pkg";
            }
            URLConnection url;
            try {
                url = new URL(se).openConnection();
                return url.getContentLengthLong() != -1;
            } catch (MalformedURLException ex) {
            } catch (IOException ex) {
            }
            return false;
        }
    }
}
