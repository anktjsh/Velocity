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
import velocity.view.DownloadsView;
import velocity.view.HistoryPane;
import velocity.view.Viewer;

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
        node.set(new DownloadsView(getEngine()));
    }

    @Override
    public void showHistory(ObjectProperty<Node> node, String url) {
        node.set(new HistoryPane(getEngine()));
    }

    @Override
    public void showPageSource(ObjectProperty<Node> node, String url, String text) {
        node.set(new Viewer(text));
    }

}
