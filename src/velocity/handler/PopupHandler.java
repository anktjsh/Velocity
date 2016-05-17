/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

import javafx.scene.web.PopupFeatures;
import velocity.core.VelocityEngine;

/**
 *
 * @author Aniket
 */
public interface PopupHandler {

    public VelocityEngine createPopup(PopupFeatures feat);

    public void launchPopup(String url);
}
