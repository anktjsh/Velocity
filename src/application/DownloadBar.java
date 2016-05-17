/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import velocity.core.Download;
import velocity.handler.PopupHandler;
import velocity.manager.DownloadManager;

/**
 *
 * @author Aniket
 */
public class DownloadBar extends BorderPane {

    private final ToolBar bar;
    private final Button close;
    PopupHandler handler;

    public DownloadBar() {
        setPadding(new Insets(5, 10, 5, 10));
        bar = new ToolBar();
        close = new Button("X");
        close.setStyle(
                "-fx-background-radius: 5em; "
                + "-fx-min-width: 30px; "
                + "-fx-min-height: 30px; "
                + "-fx-max-width: 30px; "
                + "-fx-max-height: 30px;"
        );
        close.setOnAction((e) -> {
            setCenter(null);
            setRight(null);
        });
        DownloadManager.getInstance().setDownloadListener((Download d) -> {
            bar.getItems().add(new DownloadItem(d, bar, this));
            setCenter(bar);
            setRight(close);
        });
        bar.getItems().addListener((ListChangeListener.Change<? extends Node> c) -> {
            c.next();
            if (c.getList().isEmpty()) {
                close.fire();
            }
        });
    }

    public void setPopupHandler(PopupHandler ha) {
        handler = ha;
    }
}
