/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

import velocity.view.CustomTab;

/**
 *
 * @author Aniket
 */
public interface VelocityListener {

    public CustomTab showDownloads(String url);

    public CustomTab showHistory(String url);

    public CustomTab showSettings(String url);

    public CustomTab showPageSource(String url, String text);

    public CustomTab startPage();
}
