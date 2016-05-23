/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;

/**
 *
 * @author Aniket
 */
public interface VelocityListener {

    public void showDownloads(ObjectProperty<Node> node, String url);

    public void showHistory(ObjectProperty<Node> node, String url);

    public void showSettings(ObjectProperty<Node> node, String url);

    public void showPageSource(ObjectProperty<Node> node, String url, String text);

    public void startPage(ObjectProperty<Node> node);
}
