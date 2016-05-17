/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

import com.sun.javafx.scene.control.skin.ContextMenuContent;
import com.sun.javafx.scene.control.skin.ContextMenuContent.MenuItemContainer;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import velocity.core.VelocityEngine;

/**
 *
 * @author Aniket
 */
public class ContextMenuParams {

    private final VelocityEngine engine;
    private final ContextMenuContent cmc;
    private final String linkUrl;
    private final String linkText;
    private final String srcUrl;
    private final String pageUrl;
    private final String selectionText;
    private final boolean hasImage;
    private final ObservableList<String> itemList;
    private final ObservableList<String> temp;

    public ContextMenuParams(VelocityEngine engine,
            ContextMenuContent cmc,
            String linkUrl,
            String linkText,
            String srcUrl,
            String pageUrl,
            String selectionText,
            boolean hasImage) {
        this.engine = engine;
        this.cmc = cmc;
        this.linkUrl = linkUrl;
        this.linkText = linkText;
        this.srcUrl = srcUrl;
        this.pageUrl = pageUrl;
        this.selectionText = selectionText;
        this.hasImage = hasImage;
        temp = FXCollections.observableArrayList();
        for (Node n : cmc.getItemsContainer().getChildren()) {
            if (n instanceof MenuItemContainer) {
                MenuItemContainer mic = (MenuItemContainer) n;
                temp.add(mic.getItem().getText());
            }
        }
        itemList = FXCollections.unmodifiableObservableList(temp);
    }

    public VelocityEngine getEngine() {
        return engine;
    }

    private MenuItem getItem(String text, EventHandler<ActionEvent> ha) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(ha);
        return item;
    }

    public void addItem(String text, EventHandler<ActionEvent> handler) {
        temp.add(text);
        cmc.getItemsContainer().getChildren().add(cmc.new MenuItemContainer(getItem(text, handler)));
    }

    public void addItem(int index, String text, EventHandler<ActionEvent> handler) {
        temp.add(text);
        cmc.getItemsContainer().getChildren().add(index, cmc.new MenuItemContainer(getItem(text, handler)));
    }

    public List<String> getCurrentItemList() {
        return itemList;
    }

    public boolean removeItem(String text) {
        temp.remove(text);
        for (int x = cmc.getItemsContainer().getChildren().size() - 1; x >= 0; x--) {
            if (cmc.getItemsContainer().getChildren().get(x) instanceof MenuItemContainer) {
                MenuItemContainer mic = (MenuItemContainer) cmc.getItemsContainer().getChildren().get(x);
                if (mic.getItem().getText().equals(text)) {
                    cmc.getItemsContainer().getChildren().remove(mic);
                    return true;
                }
            }
        }
        return false;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getLinkText() {
        return linkText;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getSelectionText() {
        return selectionText;
    }

    public boolean hasImage() {
        return hasImage;
    }
}
