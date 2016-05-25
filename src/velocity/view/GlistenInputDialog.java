/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.view;

import com.gluonhq.charm.glisten.control.Dialog;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 *
 * @author Aniket
 */
public class GlistenInputDialog {

    private final TextField field;
    private final Dialog<String> dialog;

    public GlistenInputDialog() {
        super();
        dialog = new Dialog<>();
        field = new TextField();
        dialog.setContent(field);
        Button yesButton = new Button("OK");
        Button noButton = new Button("Cancel");
        yesButton.setOnAction(event2 -> {
            dialog.setResult(field.getText());
            dialog.hide();
        });
        field.setOnAction(yesButton.getOnAction());
        noButton.setOnAction(event2 -> {
            dialog.setResult(null);
            dialog.hide();
        });
        dialog.setOnShown((e) -> {
            Platform.runLater(() -> field.requestFocus());
        });
        dialog.getButtons().addAll(yesButton, noButton);
    }

    public void setTitle(String s) {
        dialog.setTitleText(s);
    }

    public Optional<String> showAndWait() {
        return dialog.showAndWait();
    }

}
