/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.features;

import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import velocity.core.VelocityEngine;
import velocity.manager.FavoritesManager;

/**
 *
 * @author Aniket
 */
public class FavoritesBar extends BorderPane {

    private final HBox bar;

    public FavoritesBar(VelocityEngine engine) {
        bar = new HBox(5);
        bar.setPadding(new Insets(5));
        FavoritesManager.getInstance().getFavorites().addListener((MapChangeListener.Change<? extends String, ? extends String> change) -> {
            if (change.wasAdded()) {
                bar.getChildren().add(new FavoritesItem(change.getKey(), change.getValueAdded(), engine));
                setCenter(bar);
            } else if (change.wasRemoved()) {
                for (int x = bar.getChildren().size() - 1; x >= 0; x--) {
                    Node n = bar.getChildren().get(x);
                    if (n instanceof FavoritesItem) {
                        FavoritesItem fi = (FavoritesItem) n;
                        if ((change.getValueRemoved().equals(fi.getValue()))) {
                            bar.getChildren().remove(x);
                        }
                    }
                }
            }
        });
        bar.getChildren().addListener((ListChangeListener.Change<? extends Node> c) -> {
            c.next();
            if (c.getList().isEmpty()) {
                setCenter(null);
            }
        });
        for (String key : FavoritesManager.getInstance().getFavorites().keySet()) {
            bar.getChildren().add(new FavoritesItem(key, FavoritesManager.getInstance().getFavorites().get(key), engine));
        }
        if (bar.getChildren().size() > 0) {
            setCenter(bar);
        }
    }
}
