/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.util;

import application.velocity.Desktop;
import com.gluonhq.charm.glisten.control.Alert;
import java.util.Optional;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;
import velocity.core.VelocityCore;

/**
 *
 * @author Aniket
 */
public class DialogUtils {

    public static Optional<ButtonType> showAlert(AlertType al, Window w, String title, String head, String cont) {
        if (VelocityCore.isDesktop()) {
            javafx.scene.control.Alert ale = new javafx.scene.control.Alert(al);
            ale.initOwner(w);
            ale.setTitle(title);
            ale.setHeaderText(head);
            ale.setContentText(cont);
            if (((Stage) ale.getDialogPane().getScene().getWindow()).getIcons().isEmpty()) {
                ((Stage) ale.getDialogPane().getScene().getWindow()).getIcons().add(Desktop.web);
            }
            return ale.showAndWait();
        } else {
            Alert ale = new Alert(al);
            ale.setTitleText(title);
            ale.setContentText(head);
            return ale.showAndWait();
        }
    }
}
