/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.features;

import application.velocity.BrowserView;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import velocity.core.VelocityEngine;

/**
 *
 * @author Aniket
 */
public class FindMenuItem extends CustomMenuItem {

    private final TextField field;
    private final Button cancel;

    public FindMenuItem(BrowserView web) {
        field = new TextField();
        setContent(new HBox(field, cancel = new Button("X")));
        field.setPromptText("Find");
        field.setOnAction((e) -> {
            find(field.getText(), web.getVelocityEngine().getDocument(), web.getVelocityEngine());
        });
        cancel.setOnAction((E) -> {
        });
    }

    public final void find(String s, Document doc, VelocityEngine engine) {
        if (s != null && !s.isEmpty()) {
            if (doc != null) {
                NodeList list = doc.getElementsByTagName("*");
                for (int i = 0; i < list.getLength(); i++) {
                    if (list.item(i).getTextContent().equals(s)) {
//                        list.item(i).setTextContent("Hello");
                    }
                }
            }
        }
    }

}
