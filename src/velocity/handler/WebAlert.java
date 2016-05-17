/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

import velocity.core.VelocityEngine;

/**
 *
 * @author Aniket
 */
public class WebAlert {

    private final VelocityEngine engine;
    private final String data;

    public WebAlert(VelocityEngine engine, javafx.scene.web.WebEvent<String> event) {
        data = event.getData();
        this.engine = engine;
    }

    public VelocityEngine getEngine() {
        return engine;
    }

    public String getData() {
        return data;
    }
}
