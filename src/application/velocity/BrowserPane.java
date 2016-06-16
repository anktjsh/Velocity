/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.velocity;

import application.features.DownloadBar;
import application.view.DownloadsView;
import application.view.HistoryPane;
import application.view.PdfReader;
import application.view.SettingsPane;
import application.view.StartPage;
import application.view.TabPaneDetacher;
import java.io.File;
import java.net.URI;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import velocity.cookie.CookieManager;
import velocity.core.VelocityCore;
import velocity.manager.DownloadManager;
import velocity.manager.FavoritesManager;
import velocity.manager.HistoryManager;
import velocity.manager.SettingsManager;
import velocity.plugin.Plugin;

/**
 *
 * @author Aniket
 */
public class BrowserPane extends BorderPane {

    public static String VERSION = "1.3.1";
    private final TabPane tabs;
    private final DownloadBar bar;

    public BrowserPane() {
        VelocityCore.addPlugin(new Plugin(Plugin.PluginFormat.URL, "velocityfx://downloads", (param) -> {
            return new Pair<>(new DownloadsView(param.getValue()), "Downloads");
        }));
        VelocityCore.addPlugin(new Plugin(Plugin.PluginFormat.URL, "velocityfx://history", (param) -> {
            return new Pair<>(new HistoryPane(param.getValue()), "History");
        }));
        VelocityCore.addPlugin(new Plugin(Plugin.PluginFormat.URL, "velocityfx://settings", (param) -> {
            return new Pair<>(new SettingsPane(param.getValue()), "Settings");
        }));
        VelocityCore.addPlugin(new Plugin(Plugin.PluginFormat.FILE_EXTENSION, ".pdf", (param) -> {
            File f = new File(URI.create(param.getKey()));
            return new Pair<>(new PdfReader(f), f.getName());
        }));
        VelocityCore.addPlugin(new Plugin(Plugin.PluginFormat.URL, "", (param) -> {
            return new Pair<>(new StartPage(param.getValue()), "New Tab");
        }));
        VelocityCore.addPlugin(new Plugin(Plugin.PluginFormat.URL, "about:blank", (param) -> {
            return new Pair<>(new StartPage(param.getValue()), "New Tab");
        }));
        setCenter(tabs = new TabPane());
        TabPaneDetacher.create().makeTabsDetachable(tabs);
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
            DownloadManager.getInstance().save();
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HistoryManager.getInstance().save();
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            FavoritesManager.getInstance().save();
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CookieManager.save();
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SettingsManager.save();
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
                            Platform.runLater(() -> {
                                BrowserPane.this.getScene().getWindow().fireEvent(
                                        new WindowEvent(
                                                BrowserPane.this.getScene().getWindow(),
                                                WindowEvent.WINDOW_CLOSE_REQUEST
                                        )
                                );
                            });
                            return;
                        }
                    }
                    application.velocity.BrowserView bv;
                    getTabPane().getTabs().add(index, bv = new BrowserView(""));
                    getTabPane().getSelectionModel().select(bv);
                }
            });
        }
    }
}
