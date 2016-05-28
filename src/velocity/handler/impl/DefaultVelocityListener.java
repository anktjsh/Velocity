/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import velocity.core.VelocityEngine;
import velocity.handler.VelocityListener;
import velocity.view.CustomTab;

/**
 *
 * @author Aniket
 */
public class DefaultVelocityListener extends DefaultHandler implements VelocityListener {

    public DefaultVelocityListener(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public CustomTab showDownloads(String url) {
        return null;
    }

    @Override
    public CustomTab showHistory(String url) {
        return null;
    }

    @Override
    public CustomTab showSettings(String url) {
        return null;
    }

    @Override
    public CustomTab showPageSource(String url, String text) {
        return null;
    }

    @Override
    public CustomTab startPage() {
        return null;
    }

}
