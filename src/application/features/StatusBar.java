/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.features;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Aniket
 */
public class StatusBar extends BorderPane {

    private final Label label;

    public StatusBar() {
        label = new Label();
        label.setMaxWidth(500);
        setLeft(label);
    }

    public void setText(String st) {
        label.setText(st);
    }
}
