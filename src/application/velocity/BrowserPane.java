/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.velocity;

import application.features.DownloadBar;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
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
public class BrowserPane extends BorderPane {

    public static String VERSION = "1.1.1";
    private final TabPane tabs;
    private final DownloadBar bar;

    public BrowserPane() {
        (new Thread(() -> {
            SettingsManager.initProperty("darkTheme", "false");
            SettingsManager.initProperty("downloadLocation", System.getProperty("user.home") + File.separator + "Downloads");
            SettingsManager.load();
            VelocityCore.setDefaultDownloadsLocation(SettingsManager.getProperty("downloadLocation"));
            DownloadManager.getInstance().load();
            HistoryManager.getInstance().load();
            FavoritesManager.getInstance().load();
        })).start();
        (new Thread(new AutoUpdater())).start();
        setCenter(tabs = new TabPane());
        setBottom((bar = new DownloadBar()));
        tabs.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
            if (newValue != null) {
                if (newValue instanceof BrowserView) {
                    BrowserView v = (BrowserView) newValue;
                    bar.setPopupHandler(v.getVelocityEngine().getPopupHandler());
                } else {
                    bar.setPopupHandler(null);
                }
            } else {
                bar.setPopupHandler(null);
            }
        });
        tabs.getTabs().add(new BrowserView("https://www.google.com"));
        tabs.getTabs().add(new AddTab());
        VelocityCore.setFileLauncher((f) -> {
            Desktop.host.showDocument(f.toURI().toString());
        });
        VelocityCore.setCookieListener((cookie) -> {
            return true;
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SettingsManager.save();
            DownloadManager.getInstance().save();
            HistoryManager.getInstance().save();
            FavoritesManager.getInstance().save();
        }));
    }

    public void close() {
        for (int x = tabs.getTabs().size() - 1; x >= 0; x--) {
            Event.fireEvent(tabs.getTabs().get(x), new Event(Tab.TAB_CLOSE_REQUEST_EVENT));
        }
    }

    public void resolve(KeyEvent key) {
        if (key.isControlDown()) {
            BrowserView bv = getBrowser();
            if (bv != null) {
                if (key.isShiftDown()) {
                    if (key.getCode() == KeyCode.Q) {
                        bv.exit();
                    }
                } else if (null != key.getCode()) {
                    switch (key.getCode()) {
                        case T:
                            bv.newTab();
                            break;
                        case H:
                            bv.history();
                            break;
                        case J:
                            bv.downloads();
                            break;
                        case P:
                            bv.print();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private BrowserView getBrowser() {
        Tab b = tabs.getSelectionModel().getSelectedItem();
        if (b != null) {
            if (b instanceof BrowserView) {
                return (BrowserView) b;
            }
        }
        return null;
    }

    public void loadFiles(List<File> af) {
        for (File f : af) {
            tabs.getTabs().add(tabs.getTabs().size() - 1, new BrowserView(f.toURI().toString()));
        }
    }

    public class AddTab extends Tab {

        public AddTab() {
            super("+");
            setStyle("-fx-font-size:16;");
            setClosable(false);
            selectedProperty().addListener((ob, older, ewer) -> {
                if (ewer) {
                    int index = getTabPane().getTabs().indexOf(AddTab.this);
                    if (index == 0) {
                        if (VelocityCore.isDesktop()) {
                            Desktop.close(BrowserPane.this);
                        }
                    }
                    application.velocity.BrowserView bv;
                    getTabPane().getTabs().add(index, bv = new BrowserView(""));
                    getTabPane().getSelectionModel().select(bv);
                }
            });
        }
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
                            Optional<ButtonType> o = DialogUtils.showAlert(AlertType.CONFIRMATION, getScene() == null ? null : getScene().getWindow(), "Update", "An Update to Velocity to available!\nWould you like to restart Velocity?", "");
                            if (o.isPresent()) {
                                if (o.get() == ButtonType.OK) {
                                    (new Thread(() -> {
                                        update(d.getLocalFile());
                                        Desktop.close(BrowserPane.this);
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
