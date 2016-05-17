/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.view;

import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Aniket
 */
public class Viewer extends BorderPane {

    private final TextArea area;

    public Viewer(String temp) {
        area = new TextArea();
        area.setText(temp);
        setCenter(area);
    }
}
