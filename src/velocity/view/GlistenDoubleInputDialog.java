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
import javafx.scene.layout.VBox;
import javafx.util.Pair;

/**
 *
 * @author Aniket
 */
public class GlistenDoubleInputDialog {

    private final TextField key, value;
    private final Dialog<Pair<String, String>> dialog;

    public GlistenDoubleInputDialog() {
        super();
        dialog = new Dialog<>();
        dialog.setContent(new VBox(10, key = new TextField(),
                value = new TextField()));
        Button yesButton = new Button("OK");
        Button noButton = new Button("Cancel");
        yesButton.setOnAction(event2 -> {
            dialog.setResult(new Pair<>(key.getText(), value.getText()));
            dialog.hide();
        });
        key.setOnAction(yesButton.getOnAction());
        value.setOnAction(yesButton.getOnAction());
        noButton.setOnAction(event2 -> {
            dialog.setResult(null);
            dialog.hide();
        });
        dialog.setOnShown((e) -> {
            Platform.runLater(() -> value.requestFocus());
        });
        dialog.getButtons().addAll(yesButton, noButton);
    }

    public Button getConfirmButton() {
        return (Button) dialog.getButtons().get(0);
    }

    public void setTitle(String s) {
        dialog.setTitleText(s);
    }

    public Optional<Pair<String, String>> showAndWait() {
        return dialog.showAndWait();
    }

    public TextField keyField() {
        return key;
    }

    public TextField valueField() {
        return value;
    }
}
