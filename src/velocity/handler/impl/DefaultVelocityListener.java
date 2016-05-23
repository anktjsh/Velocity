/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import velocity.core.VelocityEngine;
import velocity.handler.VelocityListener;

/**
 *
 * @author Aniket
 */
public class DefaultVelocityListener extends DefaultHandler implements VelocityListener {

    public DefaultVelocityListener(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public void showDownloads(ObjectProperty<Node> node, String url) {
    }

    @Override
    public void showHistory(ObjectProperty<Node> node, String url) {
    }

    @Override
    public void showPageSource(ObjectProperty<Node> node, String url, String text) {
    }

    @Override
    public void showSettings(ObjectProperty<Node> node, String url) {
    }

    @Override
    public void startPage(ObjectProperty<Node> node) {
    }

}
