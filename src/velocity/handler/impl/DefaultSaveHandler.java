/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import velocity.handler.DownloadResult;
import velocity.core.VelocityEngine;
import velocity.handler.SaveHandler;

/**
 *
 * @author Aniket
 */
public class DefaultSaveHandler extends DefaultHandler implements SaveHandler {

    public DefaultSaveHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public DownloadResult automaticDownload(String url, String contentType, String name) {
        return null;
    }

    @Override
    public DownloadResult saveAs(String url) {
        return null;
    }

    @Override
    public DownloadResult downloadImage(String url) {
        return null;
    }

}
