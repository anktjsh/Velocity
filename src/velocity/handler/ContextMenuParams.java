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
    private final boolean isLink;
    private final ObservableList<String> itemList;
    private final ObservableList<String> temp;

    public ContextMenuParams(VelocityEngine engine,
            ContextMenuContent cmc,
            String linkUrl,
            String linkText,
            String srcUrl,
            String pageUrl,
            String selectionText,
            boolean hasImage,
            boolean isLink) {
        this.engine = engine;
        this.cmc = cmc;
        this.linkUrl = linkUrl;
        this.linkText = linkText;
        this.srcUrl = srcUrl;
        this.pageUrl = pageUrl;
        this.selectionText = selectionText;
        this.hasImage = hasImage;
        this.isLink = isLink;
        temp = FXCollections.observableArrayList();
        for (int x = cmc.getItemsContainer().getChildren().size() - 1; x >= 0; x--) {
            if (cmc.getItemsContainer().getChildren().get(x) instanceof MenuItemContainer) {
                MenuItemContainer mic = (MenuItemContainer) cmc.getItemsContainer().getChildren().get(x);
                switch (mic.getItem().getText()) {
                    case "Go Back":
                        cmc.getItemsContainer().getChildren().set(x, cmc.new MenuItemContainer(getBackItem()));
                        break;
                    case "Go Forward":
                        cmc.getItemsContainer().getChildren().set(x, cmc.new MenuItemContainer(getForwardItem()));
                        break;
                    case "Stop loading":
                        cmc.getItemsContainer().getChildren().set(x, cmc.new MenuItemContainer(getStopMenuItem()));
                        break;
                    case "Reload page":
                        cmc.getItemsContainer().getChildren().set(x, cmc.new MenuItemContainer(getReloadMenuItem()));
                        break;
//                    case "Open Image in New Window":
//                        cmc.getItemsContainer().getChildren().set(x, cmc.new MenuItemContainer(newImageWindow()));
//                        cmc.getItemsContainer().getChildren().add(x, cmc.new MenuItemContainer(newImageTab()));
//                        break;
//                    case "Open Link in New Window":
//                        cmc.getItemsContainer().getChildren().set(x, cmc.new MenuItemContainer(newLinkWindow()));
//                        cmc.getItemsContainer().getChildren().add(x, cmc.new MenuItemContainer(newLinkTab()));
//                        break;
                    default:
                        break;
                }
                temp.add(mic.getItem().getText());
            }
        }
        itemList = FXCollections.unmodifiableObservableList(temp);
    }

//    private MenuItem newImageTab() {
//        MenuItem item = new MenuItem("Open Image in New Tab");
//        item.setOnAction((e) -> {
//            engine.launchPopupInTab(srcUrl);
//        });
//        return item;
//    }
//
//    private MenuItem newImageWindow() {
//        MenuItem item = new MenuItem("Open Image in New Window");
//        item.setOnAction((e) -> {
//            engine.launchPopupInWindow(srcUrl);
//        });
//        return item;
//    }
//
//    private MenuItem newLinkTab() {
//        MenuItem item = new MenuItem("Open Link in New Tab");
//        item.setOnAction((e) -> {
//            engine.launchPopupInTab(linkUrl);
//        });
//        return item;
//    }
//
//    private MenuItem newLinkWindow() {
//        MenuItem item = new MenuItem("Open Link in New Window");
//        item.setOnAction((e) -> {
//            engine.launchPopupInWindow(linkUrl);
//        });
//        return item;
//    }

    private MenuItem getStopMenuItem() {
        MenuItem item = new MenuItem("Stop loading");
        item.setOnAction((E) -> {
            engine.stopLoad();
        });
        return item;
    }

    private MenuItem getReloadMenuItem() {
        MenuItem item = new MenuItem("Reload page");
        item.setOnAction((E) -> {
            engine.refreshPage();
        });
        return item;
    }

    private MenuItem getBackItem() {
        MenuItem item = new MenuItem("Go Back");
        item.setOnAction((E) -> {
            engine.getHistory().go(-1);
        });
        return item;
    }

    private MenuItem getForwardItem() {
        MenuItem item = new MenuItem("Go Forward");
        item.setOnAction((E) -> {
            engine.getHistory().go(1);
        });
        return item;
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

    public boolean isLink() {
        return isLink;
    }
}
