/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import javafx.scene.Scene;
import javafx.scene.web.PopupFeatures;
import javafx.stage.Stage;
import velocity.core.VelocityEngine;
import velocity.core.VelocityView;
import velocity.handler.PopupHandler;

/**
 *
 * @author Aniket
 */
public class DefaultPopupHandler extends DefaultHandler implements PopupHandler {

    public DefaultPopupHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public VelocityEngine newWindow() {
        Stage st = new Stage();
        VelocityView vv;
        st.initOwner(getEngine().getVelocityView().getScene() == null ? null : getEngine().getVelocityView().getScene().getWindow());
        st.setScene(new Scene(vv = new VelocityView()));
        st.show();
        return vv.getEngine();
    }

    @Override
    public VelocityEngine newTab() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VelocityEngine newWindow(PopupFeatures feat) {
        return newWindow();
    }

}
