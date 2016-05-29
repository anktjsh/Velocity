/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.velocity;

import application.features.FavoritesBar;
import application.features.StatusBar;
import application.features.ZoomMenuItem;
import application.velocity.BrowserPane.AddTab;
import application.view.DownloadsView;
import application.view.HistoryPane;
import application.view.SettingsPane;
import application.view.StartPage;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.print.Printer;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.PopupFeatures;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;
import velocity.core.VelocityCore;
import velocity.core.VelocityEngine;
import velocity.core.VelocityView;
import velocity.handler.DownloadResult;
import velocity.handler.LoadListener;
import velocity.handler.PopupHandler;
import velocity.handler.PrintStatus;
import velocity.handler.SaveHandler;
import velocity.handler.VelocityListener;
import velocity.manager.FavoritesManager;
import velocity.util.DialogUtils;
import velocity.util.FileUtils;
import velocity.view.CustomTab;
import velocity.view.GlistenChoiceDialog;
import velocity.view.GlistenDoubleInputDialog;
import velocity.view.Viewer;

/**
 *
 * @author Aniket
 */
public class BrowserView extends Tab implements Serializable {

    Node refreshGraphic = VelocityCore.isDesktop() ? (new ImageView(new Image(BrowserView.class.getResourceAsStream("reload.png"), 20, 20, true, true))) : MaterialDesignIcon.REFRESH.graphic();//;
    Node cancelGraphic = VelocityCore.isDesktop() ? (new ImageView(new Image(BrowserView.class.getResourceAsStream("cancel.png"), 20, 20, true, true))) : MaterialDesignIcon.CANCEL.graphic();//;
    private final Node incognito = new ImageView(new Image(BrowserView.class.getResourceAsStream("incognito.png"), 20, 20, true, true));
    private final VelocityView view;
    private final TextField field;
    private final HBox bar;
    private final Button back, forward, refresh, favorite;
    private final MenuButton options;
    private final CustomMenuItem zoomItem;
    private final BorderPane main, top;
    private final StatusBar status;
    private final ContextMenu menu;
    private final CheckBox dialog, popup;
    private String lastTyped;

    public BrowserView(String url, boolean incog) {
        super("New Tab");
        view = new VelocityView();
        if (VelocityCore.isDesktop()) {
            view.getEngine().enableJavaScript();
        }
        view.getEngine().incognitoProperty().addListener((ob, oler, newer) -> {
            if (newer) {
                setGraphic(incognito);
            } else {
                setGraphic(null);
            }
        });
        view.getEngine().setIncognito(incog);
        menu = new ContextMenu();
        setContextMenu(menu);
        menu.getItems().addAll(new MenuItem("New Tab"),
                new MenuItem("New Incognito Tab"),
                new MenuItem("Reload"),
                new MenuItem("Close Tab"),
                new MenuItem("Close Other Tabs"),
                new CustomMenuItem(popup = new CheckBox("Disable Popups")),
                new CustomMenuItem(dialog = new CheckBox("Disable Dialogs")));
        for (MenuItem mu : menu.getItems()) {
            if (mu instanceof CustomMenuItem) {
                CustomMenuItem cmi = (CustomMenuItem) mu;
                cmi.setHideOnClick(false);
            }
        }
        menu.getItems().get(0).setOnAction((e) -> {
            view.getEngine().launchPopup("");
        });
        menu.getItems().get(1).setOnAction((e) -> {
            getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, new BrowserView("", true));
            getTabPane().getSelectionModel().select(getTabPane().getTabs().indexOf(BrowserView.this) + 1);
        });
        menu.getItems().get(2).setOnAction((e) -> {
            view.getEngine().refreshPage();
        });
        menu.getItems().get(3).setOnAction((e) -> {
            Event.fireEvent(this, new Event(Tab.TAB_CLOSE_REQUEST_EVENT));
            getTabPane().getTabs().remove(this);
        });
        menu.getItems().get(4).setOnAction((e) -> {
            int index = getTabPane().getTabs().indexOf(this);
            for (int x = getTabPane().getTabs().size() - 1; x >= 0; x--) {
                if (index != x) {
                    if (getTabPane().getTabs().get(x) instanceof AddTab) {

                    } else {
                        Event.fireEvent(getTabPane().getTabs().get(x), new Event(Tab.TAB_CLOSE_REQUEST_EVENT));
                        getTabPane().getTabs().remove(x);
                    }
                }
            }
        });
        view.getEngine().dialogsSuppressedProperty().addListener((ob, older, neweer) -> {
            dialog.setSelected(neweer);
        });
        view.getEngine().popupsSuppressedProperty().addListener((ob, older, neweer) -> {
            popup.setSelected(neweer);
        });
        dialog.selectedProperty().addListener((ob, older, newer) -> {
            view.getEngine().setDialogsSuppressed(newer);
        });
        popup.selectedProperty().addListener((ob, older, newer) -> {
            view.getEngine().setPopupsSuppressed(newer);
        });
        main = new BorderPane();
        top = new BorderPane();
        main.setTop(top);
        field = new TextField();
        options = new MenuButton("");
        options.setGraphic(VelocityCore.isDesktop() ? new ImageView(new Image(getClass().getResourceAsStream("gear.png"), 20, 20, true, true))
                : MaterialDesignIcon.SETTINGS.graphic());
        bar = new HBox(5);
        bar.setPadding(new Insets(5));
        refresh = new Button();
        Tooltip t;
        Tooltip.install(refresh, t = new Tooltip());
        refresh.setGraphic(refreshGraphic);
        view.getEngine().locationProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                field.setText(newer);
                if (newer.startsWith("velocityfx://")) {
                    refresh.setGraphic(refreshGraphic);
                }
                if (getText().equals("New Tab")) {
                    if (!newer.equals("about:blank")) {
                        if (newer.length() > 30) {
                            setText(newer.substring(0, 30));
                        } else {
                            setText(newer);
                        }
                    }
                }
            }
        });
        view.getEngine().setSaveHandler(new SaveHandler() {

            @Override
            public DownloadResult automaticDownload(String url, String contentType, String name) {
                String filename, extension;
                if (name == null) {
                    filename = url.substring(url.lastIndexOf('/') + 1);
                } else {
                    filename = name;
                }
                extension = filename.substring(filename.lastIndexOf('.') + 1);
                String sa = VelocityCore.getDefaultDownloadsLocation() + File.separator + filename.substring(0, filename.lastIndexOf('.'));
                File f = FileUtils.newFile(sa + "." + extension);
                int x = 1;
                while (f.exists()) {
                    f = FileUtils.newFile(sa + " (" + x + ")" + "." + extension);
                    x++;
                }
                return new DownloadResult(f, DownloadResult.CUSTOM);
            }

            @Override
            public DownloadResult saveAs(String url) {
                FileChooser fc = new FileChooser();
                if (url.contains("/")) {
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    if (fileName.contains(".")) {
                        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
                        File f = FileUtils.newFile("hello." + extension);
                        String type = null;
                        try {
                            type = Files.probeContentType(f.toPath());
                        } catch (IOException ex) {
                        }
                        if (type != null) {
                            fc.getExtensionFilters().add(new ExtensionFilter(extension.toUpperCase() + " File", "*." + extension));
                        }
                        fc.setInitialFileName(fileName);
                    }
                }
                fc.getExtensionFilters().add(new ExtensionFilter("Complete WebPage", "*.html"));
                fc.getExtensionFilters().add(new ExtensionFilter("HTML File", "*.html"));
                if (fc.getExtensionFilters().get(0).getDescription().startsWith("Complete")) {
                    fc.setInitialFileName("index.html");
                }
                File file = fc.showSaveDialog(getTabPane().getScene().getWindow());
                if (file == null) {
                    return new DownloadResult(null, -1);
                }
                if (fc.getSelectedExtensionFilter().getExtensions().contains("*.html")) {
                    if (fc.getSelectedExtensionFilter().getDescription().contains("HTML")) {
                        return new DownloadResult(file, DownloadResult.HTML);
                    } else {
                        return new DownloadResult(file, DownloadResult.COMPLETE);
                    }
                } else {
                    return new DownloadResult(file, DownloadResult.CUSTOM);
                }
            }

            @Override
            public DownloadResult downloadImage(String url) {
                String filename = url.substring(url.lastIndexOf('/') + 1);
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*." + url.substring(url.lastIndexOf('.') + 1)));
                fc.setInitialFileName(filename);
                return new DownloadResult(fc.showSaveDialog(getTabPane().getScene().getWindow()), DownloadResult.CUSTOM);
            }
        });
        view.getEngine().setViewSourceHandler((VelocityEngine engine) -> {
            BrowserView bv;
            getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, bv = new BrowserView(""));
            getTabPane().getSelectionModel().select(getTabPane().getTabs().indexOf(BrowserView.this) + 1);
            return bv.view.getEngine();
        });
        view.getEngine().setPrintHandler((job) -> {
            if (VelocityCore.isDesktop()) {
                List<String> choices = new ArrayList<>();
                for (Printer p : Printer.getAllPrinters()) {
                    choices.add(p.getName());
                }
                ChoiceDialog<String> printer = new ChoiceDialog<>(Printer.getDefaultPrinter().getName(), choices);
                printer.setTitle("Printer Selection");
                printer.setHeaderText("Select a Printer");
                printer.initOwner(getTabPane().getScene().getWindow());
                Optional<String> show = printer.showAndWait();
                if (show.isPresent()) {
                    Printer select = null;
                    for (Printer p : Printer.getAllPrinters()) {
                        if (p.getName().equals(show.get())) {
                            select = p;
                        }
                    }
                    if (select != null) {
                        job.setPrinter(select);
                        return PrintStatus.CONTINUE;
                    } else {
                        return PrintStatus.CANCEL;
                    }
                } else {
                    return PrintStatus.CANCEL;
                }
            } else {
                GlistenChoiceDialog printer = new GlistenChoiceDialog();
                List<String> choices = new ArrayList<>();
                for (Printer p : Printer.getAllPrinters()) {
                    choices.add(p.getName());
                }
                printer.setItems(choices);
                printer.setSelectedValue(Printer.getDefaultPrinter().getName());
                printer.setTitle("Select a Printer");
                Optional<String> show = printer.showAndWait();
                if (show.isPresent()) {
                    Printer select = null;
                    for (Printer p : Printer.getAllPrinters()) {
                        if (p.getName().equals(show.get())) {
                            select = p;
                        }
                    }
                    if (select != null) {
                        job.setPrinter(select);
                        return PrintStatus.CONTINUE;
                    } else {
                        return PrintStatus.CANCEL;
                    }
                } else {
                    return PrintStatus.CANCEL;
                }
            }
        });
        view.getEngine().setPopupHandler(new PopupHandler() {
            @Override
            public VelocityEngine createPopup(PopupFeatures feat) {
                BrowserView view = new BrowserView("");
                getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, view);
                getTabPane().getSelectionModel().select(view);
                return view.view.getEngine();
            }

            @Override
            public void launchPopup(String url) {
                getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, new BrowserView(url));
                getTabPane().getSelectionModel().select(getTabPane().getTabs().indexOf(BrowserView.this) + 1);
            }
        });
        view.getEngine().setVelocityListener(new VelocityListener() {

            @Override
            public CustomTab showDownloads(String url) {
                return new DownloadsView(view.getEngine());
            }

            @Override
            public CustomTab showHistory(String url) {
                return new HistoryPane(view.getEngine());
            }

            @Override
            public CustomTab showSettings(String url) {
                return new SettingsPane(view.getEngine());
            }

            @Override
            public CustomTab showPageSource(String url, String text) {
                return new Viewer(text);
            }

            @Override
            public CustomTab startPage() {
                return new StartPage(view.getEngine());
            }
        });
        bar.getChildren().addAll(back = new Button("", VelocityCore.isDesktop() ? new ImageView(new Image(getClass().getResourceAsStream("right.png"), 20, 20, true, true))
                : MaterialDesignIcon.ARROW_BACK.graphic()),
                forward = new Button("", VelocityCore.isDesktop() ? new ImageView(new Image(getClass().getResourceAsStream("right.png"), 20, 20, true, true))
                        : MaterialDesignIcon.ARROW_FORWARD.graphic()),
                refresh,
                field,
                favorite = new Button("Favorite"),
                options
        );
        if (back.getGraphic() instanceof ImageView) {
            ((ImageView) back.getGraphic()).setRotate(180);
        }
        bar.widthProperty().addListener((ob, older, newer) -> {
            field.setMinWidth(newer.doubleValue() / 2);
        });
        back.setDisable(true);
        forward.setDisable(true);
        view.getEngine().canGoBackProperty().addListener((ob, older, newer) -> {
            if (newer) {
                back.setDisable(false);
            } else {
                back.setDisable(true);
            }
        });
        view.getEngine().canGoForwardProperty().addListener((ob, older, newer) -> {
            if (newer) {
                forward.setDisable(false);
            } else {
                forward.setDisable(true);
            }
        });
        favorite.setText("");
        favorite.setGraphic(VelocityCore.isDesktop() ? new ImageView(new Image(getClass().getResourceAsStream("star.png"), 20, 20, true, true)) : MaterialDesignIcon.STAR.graphic());
        favorite.setOnAction((e) -> {
            if (view.getEngine().getDocument() != null) {
                if (VelocityCore.isDesktop()) {
                    Dialog<Pair<String, String>> input = new Dialog<>();
                    input.setTitle("Favorites Item");
                    input.setHeaderText("What would you like to save this link as?");
                    input.initOwner(getTabPane().getScene().getWindow());
                    ButtonType loginButtonType = new ButtonType("Confirm", ButtonData.OK_DONE);
                    input.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20, 150, 10, 10));
                    TextField username = new TextField();
                    username.setPromptText("Name");
                    TextField password = new TextField();
                    password.setPromptText("URL");
                    password.setEditable(false);
                    password.setText(view.getEngine().getLocation());
                    grid.add(new Label("Website Name:"), 0, 0);
                    grid.add(username, 1, 0);
                    grid.add(new Label("Website URL:"), 0, 1);
                    grid.add(password, 1, 1);
                    Node loginButton = input.getDialogPane().lookupButton(loginButtonType);
                    loginButton.setDisable(true);
                    username.textProperty().addListener((observable, oldValue, newValue) -> {
                        loginButton.setDisable(newValue.trim().isEmpty());
                    });
                    username.setText(view.getEngine().getTitle());
                    input.getDialogPane().setContent(grid);
                    Platform.runLater(() -> username.requestFocus());
                    input.setResultConverter(dialogButton -> {
                        if (dialogButton == loginButtonType) {
                            return new Pair<>(username.getText(), password.getText());
                        }
                        return null;
                    });
                    Optional<Pair<String, String>> result = input.showAndWait();
                    result.ifPresent(res -> {
                        boolean b = FavoritesManager.getInstance().add(res.getKey(), res.getValue());
                        if (!b) {
                            DialogUtils.showAlert(AlertType.ERROR, getTabPane().getScene().getWindow(), "Favorites", "Favorite already exists!", "");
                        } else {
                            DialogUtils.showAlert(AlertType.INFORMATION, getTabPane().getScene().getWindow(), "Favorites", "Favorite added successfully", "");
                        }
                    });
                } else {
                    GlistenDoubleInputDialog input = new GlistenDoubleInputDialog();
                    input.setTitle("Favorites Item");
                    input.keyField().setPromptText("Name");
                    input.valueField().setText(view.getEngine().getLocation());
                    input.valueField().setPromptText("URL");
                    input.getConfirmButton().setDisable(true);
                    input.keyField().textProperty().addListener((observable, oldValue, newValue) -> {
                        input.getConfirmButton().setDisable(newValue.trim().isEmpty());
                    });
                    input.keyField().setText(view.getEngine().getTitle());
                    Optional<Pair<String, String>> result = input.showAndWait();
                    result.ifPresent(res -> {
                        boolean b = FavoritesManager.getInstance().add(res.getKey(), res.getValue());
                        if (!b) {
                            DialogUtils.showAlert(AlertType.ERROR, getTabPane().getScene().getWindow(), "Favorites", "Favorite already exists!", "");
                        } else {
                            DialogUtils.showAlert(AlertType.INFORMATION, getTabPane().getScene().getWindow(), "Favorites", "Favorite added successfully", "");
                        }
                    });
                }
            }
        });
        options.getItems().addAll(new MenuItem("New Tab\t\tCtrl+T"),
                new MenuItem("New Incognito Tab"),
                new MenuItem("History\t\tCtrl+H"),
                new MenuItem("Downloads\tCtrl+J"),
                zoomItem = new ZoomMenuItem(view),
                new MenuItem("Save File As"),
                new MenuItem("Print\t\t\tCtrl+P"),
                new MenuItem("Settings"),
                new MenuItem("About"),
                new MenuItem("Exit\t\tCtrl+Shift+Q"));
        options.getItems().get(0).setOnAction((e) -> {
            newTab();
        });
        options.getItems().get(1).setOnAction((E) -> {
            getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, new BrowserView("", true));
            getTabPane().getSelectionModel().select(getTabPane().getTabs().indexOf(BrowserView.this) + 1);
        });
        options.getItems().get(2).setOnAction((e) -> {
            history();
        });
        options.getItems().get(3).setOnAction((e) -> {
            downloads();
        });
        options.getItems().get(5).setOnAction((E) -> {
            view.getEngine().saveAs(view.getEngine().getLocation());
        });
        options.getItems().get(6).setOnAction((e) -> {
            print();
        });
        options.getItems().get(7).setOnAction((e) -> {
            settings();
        });
        options.getItems().get(8).setOnAction((e) -> {
            DialogUtils.showAlert(AlertType.INFORMATION, getTabPane().getScene().getWindow(),
                    "Velocity Information",
                    VelocityCore.isDesktop()
                            ? "Velocity v1.0.0" : "Velocity v1.0.0\nCreated by Aniket Joshi",
                    VelocityCore.isDesktop() ? "Created by Aniket Joshi" : "");
        });
        options.getItems().get(9).setOnAction((e) -> {
            exit();
        });
        main.setCenter(view);
        main.setBottom(status = new StatusBar());
        view.getEngine().setStatusListener((status1) -> {
            status.setText(status1);
        });
        top.setCenter(bar);
        top.setBottom(new FavoritesBar(view.getEngine()));
        setContent(main);
        view.getEngine().titleProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                if (newer.length() > 30) {
                    setText(newer.substring(0, 30));
                } else {
                    setText(newer);
                }
            }
        });
        if (VelocityCore.isDesktop()) {
            String os = System.getProperty("os.name").toLowerCase();
            view.getEngine().setUserAgent(view.getEngine().getUserAgent() + " Chrome/50.0.2661.102");
        }
        view.getEngine().setProgressListener((double progress) -> {
            t.setText((progress * 100) + "%");
            if (progress == 1) {
                refresh.setGraphic(refreshGraphic);
            } else if (refresh.getGraphic() != cancelGraphic) {
                refresh.setGraphic(cancelGraphic);
            }
        });
        forward.setOnAction((e) -> {
            view.getEngine().goForward();
        });
        back.setOnAction((E) -> {
            view.getEngine().goBack();
        });
        refresh.setOnAction((e) -> {
            if (refresh.getGraphic() == refreshGraphic) {
                view.getEngine().refreshPage();
                refresh.setGraphic(cancelGraphic);
            } else {
                view.getEngine().stopLoad();
                refresh.setGraphic(refreshGraphic);
            }
        });
        view.getEngine().setLoadListener(new LoadListener() {
            @Override
            public void onLoadCompleted() {
                //take snapshot
                //thumbnail
            }

            @Override
            public void onLoadCancelled() {
            }

            @Override
            public void onLoadReady() {
            }

            @Override
            public void onLoadFailed() {
                if (lastTyped != null) {
                    view.getEngine().load("https://www.google.com/search?q=" + lastTyped + "&oq=" + lastTyped + "&aqs=chrome..69i57j0l2j69i65j0l2.1918j0j7&sourceid=chrome&ie=UTF-8");
                } else if (!(view.getEngine().getLocation().isEmpty() || view.getEngine().getLocation().equals("about:blank"))) {
                    DialogUtils.showAlert(AlertType.WARNING, getTabPane() != null ? getTabPane().getScene().getWindow() : null, "Internet", "You are not connected to the internet!", "");
                }
            }

            @Override
            public void onLoadScheduled() {
            }

            @Override
            public void onLoadRunning() {
            }
        });
        field.setOnAction((e) -> {
            (new Thread(() -> {
                if (field.getText().equals(view.getEngine().getLocation())) {
                    Platform.runLater(()
                            -> load(field.getText()));
                } else {
                    String s = field.getText();
                    lastTyped = s;
                    if (s.contains(" ")) {
                        Platform.runLater(()
                                -> view.getEngine().load("https://www.google.com/search?q=" + s + "&oq=" + s + "&aqs=chrome..69i57j0l2j69i65j0l2.1918j0j7&sourceid=chrome&ie=UTF-8"));
                    } else {
                        Platform.runLater(()
                                -> load(s));
                    }
                    Platform.runLater(()
                            -> field.getParent().requestFocus());
                }
            })).start();
        });
        setOnClosed((E) -> {
            view.dispose();
        });
        view.getEngine().load(url);
    }

    public BrowserView(String url) {
        this(url, false);
    }

    public final void newTab() {
        getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, new BrowserView(""));
        getTabPane().getSelectionModel().select(getTabPane().getTabs().indexOf(BrowserView.this) + 1);
    }

    public final void history() {
        getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, new BrowserView("velocityfx://history"));
        getTabPane().getSelectionModel().select(getTabPane().getTabs().indexOf(BrowserView.this) + 1);
    }

    public final void settings() {
        getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, new BrowserView("velocityfx://settings"));
        getTabPane().getSelectionModel().select(getTabPane().getTabs().indexOf(BrowserView.this) + 1);
    }

    public final void downloads() {
        getTabPane().getTabs().add(getTabPane().getTabs().indexOf(BrowserView.this) + 1, new BrowserView("velocityfx://downloads"));
        getTabPane().getSelectionModel().select(getTabPane().getTabs().indexOf(BrowserView.this) + 1);
    }

    public final void print() {
        view.getEngine().print();
    }

    public final void exit() {
        Desktop.close((BrowserPane) getTabPane().getParent());
    }

    private void load(String url) {
        if (!url.isEmpty()) {
            if (!url.contains(":")) {
                url = "https://" + url;
            }
            view.getEngine().load(url);
        } else {
            view.getEngine().load(url);
        }
    }

    public VelocityEngine getVelocityEngine() {
        return view.getEngine();
    }

}
