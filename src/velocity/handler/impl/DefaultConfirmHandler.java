/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import velocity.core.VelocityCore;
import velocity.core.VelocityEngine;
import velocity.handler.ConfirmHandler;

/**
 *
 * @author Aniket
 */
public class DefaultConfirmHandler extends DefaultHandler implements ConfirmHandler {

    public DefaultConfirmHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public Boolean call(String param) {
        if (VelocityCore.isDesktop()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(param);
            alert.initOwner(getEngine().getVelocityView().getScene() == null ? null : getEngine().getVelocityView().getScene().getWindow());
            CheckBox box = new CheckBox("Stop showing any more dialogs");
            box.selectedProperty().addListener((ob, older, newer) -> {
                getEngine().setDialogsSuppressed(newer);
            });
            alert.getDialogPane().setExpandableContent(box);
            alert.getDialogPane().setExpanded(true);
            Optional<ButtonType> result = alert.showAndWait();
            return result.get() == ButtonType.OK;
        } else {
            com.gluonhq.charm.glisten.control.Alert alert = new com.gluonhq.charm.glisten.control.Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitleText("Confirmation");
            alert.setContentText(param);
            Optional<ButtonType> result = alert.showAndWait();
            return result.get() == ButtonType.OK;
        }
    }

}
