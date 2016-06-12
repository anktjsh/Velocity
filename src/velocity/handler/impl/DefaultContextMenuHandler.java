/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import velocity.core.VelocityEngine;
import velocity.handler.ContextMenuHandler;
import velocity.handler.ContextMenuParams;

/**
 *
 * @author Aniket
 */
public class DefaultContextMenuHandler extends DefaultHandler implements ContextMenuHandler {

    public DefaultContextMenuHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public void showContextMenu(ContextMenuParams params) {
        if (params.hasImage()) {
            params.addItem("Open Image", (e) -> {
                params.getEngine().load(params.getEngine().getLocation() + params.getSrcUrl());
            });
            params.addItem("Save Image", (e) -> {
                if (params.getPageUrl().equals(params.getSrcUrl())) {
                    params.getEngine().saveImage(params.getSrcUrl());
                } else {
                    params.getEngine().saveImage(params.getPageUrl() + params.getSrcUrl());
                }
            });
            params.addItem("Copy Image Address", (e) -> {
                Clipboard clip = Clipboard.getSystemClipboard();
                ClipboardContent cc = new ClipboardContent();
                cc.putUrl(params.getEngine().getLocation() + params.getSrcUrl());
                cc.putString(params.getEngine().getLocation() + params.getSrcUrl());
                clip.setContent(cc);
            });
        }
        params.addItem("Save As", (e) -> {
            params.getEngine().saveAs(params.getPageUrl());
        });
        params.addItem("Print", (e) -> {
            params.getEngine().print();
        });
        params.addItem("View Source", (e) -> {
            String html = (String) params.getEngine().executeScript("document.documentElement.outerHTML");
            getEngine().launchPopupInTab("velocityfx://source-\t" + getEngine().getLocation() + "\t" + html);
        });
    }

}
