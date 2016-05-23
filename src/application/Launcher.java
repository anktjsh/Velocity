/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import javafx.application.Application;
import javafx.stage.Stage;
import velocity.core.VelocityCore;

/**
 *
 * @author Aniket
 */
public class Launcher extends Application {

    public static void main(String args[]) {
        if (VelocityCore.isDesktop()) {
            launch(Desktop.class);
        } else {
            launch(Mobile.class);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
    }
}
