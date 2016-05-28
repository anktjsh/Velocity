/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.view;

import java.util.ArrayList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import velocity.core.VelocityEngine;
import velocity.manager.HistoryManager;
import velocity.manager.SettingsManager;
import velocity.view.CustomTab;

/**
 *
 * @author Aniket
 */
public class StartPage extends CustomTab {

    private final GridPane grid;
    private final VelocityEngine engine;
    private final SettingsManager.SettingsListener listener;

    @Override
    public void close() {
        SettingsManager.removeSettingsListener(listener);
    }

    public StartPage(VelocityEngine engine) {
        if (!Boolean.parseBoolean(SettingsManager.getProperty("darkTheme"))) {
            setStyle("-fx-background-color:white;");
        }
        SettingsManager.addSettingsListener(listener = (settingsKey, oldValue, newValue) -> {
            if (settingsKey.equals("darkTheme")) {
                if (Boolean.parseBoolean(newValue)) {
                    setStyle("");
                } else {
                    setStyle("-fx-background-color:white;");
                }
            }
        });
        this.engine = engine;
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        setCenter(grid);
        ArrayList<Pair<String, String>> al = HistoryManager.getInstance().getFrequency();
        for (int x = 0; x < 8; x++) {
            if (x < al.size()) {
                add(newButton(al.get(x).getKey(), al.get(x).getValue()), x);
            }
        }
        for (Node n : grid.getChildren()) {
            if (n instanceof Button) {
                Button b = (Button) n;
                b.setMinSize(125, 125);
                b.setMaxSize(125, 125);
            }
        }
    }

    public final Button newButton(String text, String url) {
        Button b = new Button(text.isEmpty() ? url : text);
        b.setOnAction((e) -> {
            engine.load(url);
        });
        return b;
    }

    public final void add(Node n, int pos) {
        switch (pos) {
            case 0:
                grid.add(n, 0, 0);
                break;
            case 1:
                grid.add(n, 1, 0);
                break;
            case 2:
                grid.add(n, 2, 0);
                break;
            case 3:
                grid.add(n, 3, 0);
                break;
            case 4:
                grid.add(n, 0, 1);
                break;
            case 5:
                grid.add(n, 1, 1);
                break;
            case 6:
                grid.add(n, 2, 1);
                break;
            case 7:
                grid.add(n, 3, 1);
                break;
        }
    }
}
