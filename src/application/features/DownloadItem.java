/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.features;

import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import velocity.core.Download;
import velocity.core.VelocityCore;
import velocity.util.DialogUtils;

/**
 *
 * @author Aniket
 */
public class DownloadItem extends BorderPane {

    private final ProgressBar bar;
    private final Button cancel;
    private final MenuButton file;
    private final Label error;

    public DownloadItem(Download d, HBox hbox, DownloadBar down) {
        setPadding(new Insets(5, 10, 5, 10));
        setCenter(bar = new ProgressBar());
        setRight(cancel = new Button("x"));
        file = new MenuButton(d.getLocalFile().getName());
        error = new Label("File did not download properly");
        d.stateProperty().addListener((source, oldState, newState) -> {
            if (newState.equals(Worker.State.SUCCEEDED)) {
                setCenter(file);
            } else if (newState.equals(Worker.State.FAILED)) {
                setCenter(error);
            }
        });
        cancel.setStyle(
                "-fx-background-radius: 5em; "
                + "-fx-min-width: 25px; "
                + "-fx-min-height: 25px; "
                + "-fx-max-width: 25px; "
                + "-fx-max-height: 25px;"
        );
        cancel.setOnAction((e) -> {
            if (getCenter() == bar) {
                d.cancel();
                setCenter(new Label("Download Cancelled"));
            } else if (getCenter() == error) {
                setCenter(null);
                setRight(null);
                hbox.getChildren().remove(this);
            } else if (getCenter() == file) {
                setCenter(null);
                setRight(null);
                hbox.getChildren().remove(this);
            } else if (getCenter() instanceof Label) {
                Label l = (Label) getCenter();
                if (l.getText().equals("Download Cancelled")) {
                    setCenter(null);
                    setRight(null);
                    hbox.getChildren().remove(this);
                }
            }
        });
        file.getItems().addAll(
                new MenuItem("Open File"),
                new MenuItem("Open Folder"),
                new MenuItem("Copy Url to Clipboard"),
                new MenuItem("Copy File Path to Clipboard"));
        file.getItems().get(0).setOnAction((e) -> {
            VelocityCore.FileLaunchStatus la = VelocityCore.launchFileInBrowser(d.getLocalFile(), down.handler);
            if (la == VelocityCore.FileLaunchStatus.FAILURE) {
                DialogUtils.showAlert(Alert.AlertType.ERROR, getScene().getWindow(), "File Launch", "File failed to launch", "");
            }
        });
        file.getItems().get(1).setOnAction((e) -> {
            VelocityCore.FileLaunchStatus la = VelocityCore.launchFileInBrowser(d.getLocalFile().getParentFile(), down.handler);
            if (la == VelocityCore.FileLaunchStatus.FAILURE) {
                DialogUtils.showAlert(Alert.AlertType.ERROR, getScene().getWindow(), "File Launch", "File failed to launch", "");
            }
        });
        file.getItems().get(2).setOnAction((e) -> {
            Clipboard cl = Clipboard.getSystemClipboard();
            ClipboardContent cc = new ClipboardContent();
            cc.putString(d.getRemoteUrl());
            cc.putUrl(d.getRemoteUrl());
            cl.setContent(cc);
        });
        file.getItems().get(3).setOnAction((e) -> {
            Clipboard cl = Clipboard.getSystemClipboard();
            ClipboardContent cc = new ClipboardContent();
            cc.putString(d.getLocalFile().getAbsolutePath());
            cc.putUrl(d.getLocalFile().toURI().toString());
            cl.setContent(cc);
        });
    }
}
