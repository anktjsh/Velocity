/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.features;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
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
            VelocityCore.FileLaunchStatus la = VelocityCore.launchFileInBrowser(d.getLocalFile());
            switch (la) {
                case DEFAULT:
                    if (down.handler != null) {
                        down.handler.newTab().load(d.getLocalFile().toURI().toString());
                    }
                    break;
                case CUSTOM:
                    break;
            }
        });
        file.getItems().get(1).setOnAction((e) -> {
            VelocityCore.FileLaunchStatus la = VelocityCore.launchFileInBrowser(d.getLocalFile().getParentFile());
            switch (la) {
                case DEFAULT:
                    if (down.handler != null) {
                        down.handler.newTab().load(d.getLocalFile().getParentFile().toURI().toString());
                    }
                    break;
                case CUSTOM:
                    break;
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

    private ArrayList<String> getList(File localFile) {
        Scanner in = null;
        try {
            in = new Scanner(localFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DownloadItem.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<String> al = new ArrayList<>();
        if (in != null) {
            while (in.hasNextLine()) {
                al.add(in.nextLine());
            }
        }
        return al;
    }
}
