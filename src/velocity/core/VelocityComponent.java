/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.core;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javax.swing.JPanel;

/**
 *
 * @author swatijoshi
 */
class VelocityComponent extends JPanel{
    private final JFXPanel panel;
    private final VelocityView view;
    public VelocityComponent() {
        panel = new JFXPanel();
        view = new VelocityView();
    }
    
    public void init() {
        Platform.runLater(() -> {
            panel.setScene(new Scene(view));
        });
    }
}
