/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebErrorEvent;
import velocity.core.VelocityEngine;
import velocity.handler.ErrorHandler;

/**
 *
 * @author Aniket
 */
public class DefaultErrorHandler extends DefaultHandler implements ErrorHandler {

    public DefaultErrorHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public void handle(WebErrorEvent event) {
        Alert al = new Alert(Alert.AlertType.ERROR);
        al.initOwner(getEngine().getVelocityView().getScene() == null ? null : getEngine().getVelocityView().getScene().getWindow());
        al.setTitle("Error");
        al.setHeaderText(event.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        event.getException().printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        al.getDialogPane().setExpandableContent(expContent);
        al.showAndWait();
    }

}
