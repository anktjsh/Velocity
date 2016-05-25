/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.features;

import velocity.manager.FavoritesManager;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import velocity.core.VelocityEngine;

/**
 *
 * @author Aniket
 */
public class FavoritesBar extends BorderPane {

    private final ToolBar bar;

    public FavoritesBar(VelocityEngine engine) {
        bar = new ToolBar();
        FavoritesManager.getInstance().getFavorites().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            if (change.wasAdded()) {
                bar.getItems().add(new FavoritesItem(change.getKey(), change.getValueAdded(), engine));
                setCenter(bar);
            } else if (change.wasRemoved()) {
                for (int x = bar.getItems().size() - 1; x >= 0; x--) {
                    Node n = bar.getItems().get(x);
                    if (n instanceof FavoritesItem) {
                        FavoritesItem fi = (FavoritesItem) n;
                        if ((change.getValueRemoved().equals(fi.getValue()))) {
                            bar.getItems().remove(x);
                        }
                    }
                }
            }
        });
        bar.getItems().addListener((ListChangeListener.Change<? extends Node> c) -> {
            c.next();
            if (c.getList().isEmpty()) {
                setCenter(null);
            }
        });
        for (String key : FavoritesManager.getInstance().getFavorites().keySet()) {
            bar.getItems().add(new FavoritesItem(key, FavoritesManager.getInstance().getFavorites().get(key), engine));
        }
        if (bar.getItems().size() > 0) {
            setCenter(bar);
        }
    }
}
