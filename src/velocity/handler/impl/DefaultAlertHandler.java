/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import velocity.core.VelocityEngine;
import velocity.handler.AlertHandler;
import velocity.handler.WebAlert;

/**
 *
 * @author Aniket
 */
public class DefaultAlertHandler extends DefaultHandler implements AlertHandler {

    public DefaultAlertHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public void handle(WebAlert event) {
        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.initOwner(getEngine().getVelocityView().getScene() == null ? null : getEngine().getVelocityView().getScene().getWindow());
        al.setTitle("Alert");
        al.setHeaderText(event.getData());
        CheckBox box = new CheckBox("Stop showing any more dialogs");
        box.selectedProperty().addListener((ob, older, newer) -> {
            getEngine().setDialogsSuppressed(newer);
        });
        al.getDialogPane().setExpandableContent(box);
        al.getDialogPane().setExpanded(true);
        al.showAndWait();
    }

}
