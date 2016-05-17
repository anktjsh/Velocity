/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import javafx.scene.Scene;
import javafx.stage.Stage;
import velocity.core.VelocityEngine;
import velocity.core.VelocityView;
import velocity.handler.ViewSourceHandler;

/**
 *
 * @author Aniket
 */
public class DefaultViewSourceHandler extends DefaultHandler implements ViewSourceHandler {

    public DefaultViewSourceHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public VelocityEngine viewSource(VelocityEngine engine) {
        Stage stage = new Stage();
        stage.initOwner(engine.getVelocityView().getScene() == null ? null : engine.getVelocityView().getScene().getWindow());
        VelocityView vv;
        stage.setScene(new Scene(vv = new VelocityView()));
        stage.show();
        return vv.getEngine();
    }

}
