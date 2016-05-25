/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.view;

import application.velocity.Desktop;
import com.gluonhq.charm.glisten.visual.Theme;
import java.io.File;
import java.net.CookieHandler;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import velocity.cookie.Cookie;
import velocity.core.VelocityCore;
import velocity.core.VelocityEngine;
import velocity.manager.SettingsManager;

/**
 *
 * @author Aniket
 */
public class SettingsPane extends BorderPane {

    private final Label title;
    private final VBox box;
    private final HBox downloadsBox;
    private final TextField downloadLocation;
    private final Button changeLocation;
    private final CheckBox check;
    private final ListView<String> blocked;
    private final HBox blockedBox;
    private final Button removeBlockedFromList, goToBlocked;
    private final ListView<String> cookieView;
    private final HBox cookieBox;
    private final Button removeCookie;

    public SettingsPane(VelocityEngine engine) {
        setPadding(new Insets(5, 10, 5, 10));
        setTop(title = new Label("Settings"));
        title.setFont(new Font(18));
        BorderPane.setAlignment(title, Pos.CENTER);
        setCenter(new ScrollPane(box = new VBox(10)));
        box.setAlignment(Pos.TOP_CENTER);
        box.getChildren().addAll(new Label("Downloads"),
                downloadsBox = new HBox(5));
        ((Label) box.getChildren().get(0)).setFont(new Font(15));
        downloadsBox.setAlignment(Pos.CENTER);
        downloadsBox.getChildren().addAll(
                new Label("Downloads Location"),
                downloadLocation = new TextField(VelocityCore.getDefaultDownloadsLocation()),
                changeLocation = new Button("Change..."));
        downloadLocation.setEditable(false);
        downloadLocation.setMinWidth(250);
        changeLocation.setOnAction((e) -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Downloads Location");
            File show = dc.showDialog(getScene().getWindow());
            if (show != null) {
                downloadLocation.setText(show.getAbsolutePath());
                VelocityCore.setDefaultDownloadsLocation(show.getAbsolutePath());
                SettingsManager.setProperty("downloadLocation", show.getAbsolutePath());
            }
        });
        box.getChildren().addAll(check = new CheckBox("Dark Theme"));
        check.setSelected(Boolean.parseBoolean(SettingsManager.getProperty("darkTheme")));
        check.setOnAction((e) -> {
            if (VelocityCore.isDesktop()) {
                if (check.isSelected()) {
                    getScene().getStylesheets().add(Desktop.material);
                } else {
                    getScene().getStylesheets().remove(Desktop.material);
                }
            } else {
                if (check.isSelected()) {
                    Theme.DARK.assignTo(getScene());
                } else {
                    Theme.LIGHT.assignTo(getScene());
                }
            }
            SettingsManager.setProperty("darkTheme", check.isSelected()+"");
        });
        box.getChildren().addAll(blockedBox = new HBox(5, new Label("Blocked Popups"), blocked = new ListView<>(), new VBox(10, removeBlockedFromList = new Button("Remove"),
                goToBlocked = new Button("Go To"))));
        blockedBox.setAlignment(Pos.CENTER);
        removeBlockedFromList.setDisable(true);
        goToBlocked.setDisable(true);
        blocked.setMaxSize(300, 300);
        blocked.getItems().addAll(VelocityCore.getBlockedUrls());
        blocked.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                if (blocked.getSelectionModel().getSelectedItem() != null) {
                    engine.launchPopup(blocked.getSelectionModel().getSelectedItem());
                    blocked.getItems().remove(blocked.getSelectionModel().getSelectedItem());
                }
            }
        });
        removeBlockedFromList.setOnAction((e) -> {
            if (blocked.getSelectionModel().getSelectedItem() != null) {
                blocked.getItems().remove(blocked.getSelectionModel().getSelectedItem());
                blocked.getItems().remove(blocked.getSelectionModel().getSelectedItem());
            }
        });
        goToBlocked.setOnAction((E) -> {
            if (blocked.getSelectionModel().getSelectedItem() != null) {
                engine.launchPopup(blocked.getSelectionModel().getSelectedItem());
            }
        });
        blocked.getSelectionModel().selectedItemProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                removeBlockedFromList.setDisable(false);
                goToBlocked.setDisable(false);
            } else {
                removeBlockedFromList.setDisable(true);
                goToBlocked.setDisable(true);
            }
        });
        box.getChildren().add(cookieBox = new HBox(5, new Label("Cookies"), cookieView = new ListView<>(), new VBox(10,
                removeCookie = new Button("Remove"))));
        cookieBox.setAlignment(Pos.CENTER);
        cookieView.setMaxSize(300, 300);
        List<Cookie> cookies = ((velocity.cookie.CookieManager) CookieHandler.getDefault()).getCookies();
        for (Cookie c : cookies) {
            cookieView.getItems().add(c.toString());
        }
        removeCookie.setDisable(true);
        removeCookie.setOnAction((E) -> {
            if (cookieView.getSelectionModel().getSelectedItem() != null) {
                Cookie sel = null;
                for (Cookie c : cookies) {
                    if (c.toString().equals(cookieView.getSelectionModel().getSelectedItem())) {
                        sel = c;
                    }
                }
                if (sel != null) {
                    cookies.remove(sel);
                }
            }
        });
        cookieView.getSelectionModel().selectedItemProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                removeCookie.setDisable(false);
            } else {
                removeCookie.setDisable(true);
            }
        });
        box.getChildren().addAll(new Label("Velocity v1.0.0 Created by Aniket Joshi"));
        sceneProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
//                check.setSelected(newer.getStylesheets().contains(Browser.material));
                box.setMinWidth(newer.getWidth() - 35);
            }
        });

        //certificates
        //search engine
    }
}
