/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.features;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import velocity.core.VelocityEngine;
import velocity.manager.FavoritesManager;

/**
 *
 * @author Aniket
 */
public class FavoritesItem extends Button {

    private final String value;

    public FavoritesItem(String name, String url, VelocityEngine engine) {
        setText(name);
        value = url;
        setOnAction((e) -> {
            engine.launchPopup(url);
        });
        setContextMenu(new ContextMenu(new MenuItem("Open"), new MenuItem("Open in New Tab"), new MenuItem("Delete")));
        getContextMenu().getItems().get(0).setOnAction((e) -> {
            engine.load(url);
        });
        getContextMenu().getItems().get(1).setOnAction((e) -> {
            engine.launchPopup(url);
        });
        getContextMenu().getItems().get(2).setOnAction((e) -> {
            FavoritesManager.getInstance().getFavorites().remove(name);
        });
    }

    public String getValue() {
        return value;
    }
}
