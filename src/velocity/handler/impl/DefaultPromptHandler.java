/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import java.util.Optional;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.PromptData;
import velocity.core.VelocityEngine;
import velocity.handler.PromptHandler;

/**
 *
 * @author Aniket
 */
public class DefaultPromptHandler extends DefaultHandler implements PromptHandler {

    public DefaultPromptHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public String call(PromptData param) {
        TextInputDialog dialog = new TextInputDialog(param.getDefaultValue());
        dialog.setTitle("Prompt");
        dialog.initOwner(getEngine().getVelocityView().getScene() == null ? null : getEngine().getVelocityView().getScene().getWindow());
        dialog.setHeaderText(param.getMessage());
        CheckBox box = new CheckBox("Stop showing any more dialogs");
        box.selectedProperty().addListener((ob, older, newer) -> {
            getEngine().setDialogsSuppressed(newer);
        });
        dialog.getDialogPane().setExpandableContent(box);
        dialog.getDialogPane().setExpanded(true);
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }
        return "";
    }

}
