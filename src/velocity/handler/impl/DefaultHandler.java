/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import velocity.core.VelocityEngine;

/**
 *
 * @author Aniket
 */
public class DefaultHandler {

    private final VelocityEngine engine;

    public DefaultHandler(VelocityEngine engine) {
        this.engine = engine;
    }

    public VelocityEngine getEngine() {
        return engine;
    }
}
