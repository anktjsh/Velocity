/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.view;

import com.gluonhq.charm.glisten.control.Dialog;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 *
 * @author Aniket
 */
public class GlistenChoiceDialog {

    private final Dialog<String> dialog;
    private final ComboBox<String> choices;

    public GlistenChoiceDialog() {
        super();
        dialog = new Dialog<>();
        choices = new ComboBox<>();
        dialog.setContent(choices);
        Button yesButton = new Button("OK");
        Button noButton = new Button("Cancel");
        yesButton.setOnAction(event2 -> {
            dialog.setResult(choices.getValue());
            dialog.hide();
        });
        choices.setOnAction(yesButton.getOnAction());
        noButton.setOnAction(event2 -> {
            dialog.setResult(null);
            dialog.hide();
        });
        dialog.setOnShown((e) -> {
            Platform.runLater(() -> choices.requestFocus());
        });
        dialog.getButtons().addAll(yesButton, noButton);
    }

    public void setSelectedValue(String s) {
        choices.setValue(s);
    }

    public void setItems(List<String> al) {
        choices.getItems().clear();
        choices.getItems().addAll(al);
    }

    public void setTitle(String s) {
        dialog.setTitleText(s);
    }

    public Optional<String> showAndWait() {
        return dialog.showAndWait();
    }

}
