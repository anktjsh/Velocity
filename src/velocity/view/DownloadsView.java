/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.view;

import java.util.ArrayList;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import velocity.core.Download;
import velocity.core.VelocityCore;
import velocity.core.VelocityCore.FileLaunchStatus;
import velocity.core.VelocityEngine;
import velocity.manager.DownloadManager;

/**
 *
 * @author Aniket
 */
public class DownloadsView extends BorderPane {

    private final ArrayList<Download> allEntries;
    private final ListView<Download> view;
    private final VelocityEngine engine;
    private final BorderPane top;
    private final ToolBar searchBar;
    private final Button search, clear;
    private final TextField field;

    public DownloadsView(VelocityEngine engine) {
        this.engine = engine;
        setPadding(new Insets(5, 10, 5, 10));
        view = new ListView<>();
        setTop(top = new BorderPane());
        Label ll;
        top.setTop(ll = new Label("Downloads"));
        top.setRight(searchBar = new ToolBar());
        searchBar.getItems().add(clear = new Button("Clear Downloads"));
        searchBar.getItems().addAll(field = new TextField(), search = new Button("Search"));
        ll.setFont(new Font(18));
        setCenter(view);
        view.setCellFactory((param) -> new DownloadsCell());
        allEntries = new ArrayList<>();
        for (int x = DownloadManager.getInstance().getDownloads().size() - 1; x >= 0; x--) {
            allEntries.add(DownloadManager.getInstance().getDownloads().get(x));
        }
        view.getItems().addAll(allEntries);
        field.setPromptText("Search");
        field.setOnAction((e) -> {
            view.getItems().clear();
            if (!field.getText().isEmpty()) {
                for (Download we : allEntries) {
                    if (we.getLocalFile().getAbsolutePath().contains(field.getText()) || we.getRemoteUrl().contains(field.getText())) {
                        view.getItems().add(we);
                    }
                }
            } else {
                view.getItems().addAll(allEntries);
            }
        });
        search.setOnAction(field.getOnAction());
        clear.setOnAction((e) -> {
            Alert al = new Alert(Alert.AlertType.CONFIRMATION);
            al.setTitle("Confirm");
            al.setHeaderText("Are you sure you want to clear these entries?");
            al.initOwner(getScene() == null ? null : getScene().getWindow());
            Optional<ButtonType> show = al.showAndWait();
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    DownloadManager.getInstance().clear();
                    view.getItems().clear();
                }
            }
        });
    }

    private class DownloadsCell extends ListCell<Download> {

        private final HBox box;
        private final Button open;
        private final Label name;
        private final Button cancel, delete;
        private Download down;

        public DownloadsCell() {
            box = new HBox(5);
            box.getChildren().addAll(
                    name = new Label(),
                    open = new Button("Open File"),
                    delete = new Button("Delete File"),
                    cancel = new Button("x"));
            cancel.setStyle(
                    "-fx-background-radius: 5em; "
                    + "-fx-min-width: 25px; "
                    + "-fx-min-height: 25px; "
                    + "-fx-max-width: 25px; "
                    + "-fx-max-height: 25px;"
            );
            name.setMaxWidth(400);
            name.setMinWidth(400);
            cancel.setOnAction((e) -> {
                view.getItems().remove(getItem());
            });
            delete.setOnAction(((e) -> {
                getItem().getLocalFile().delete();
            }));
            open.setOnAction((e) -> {
                if (down != null) {
                    FileLaunchStatus la = VelocityCore.launchFileInBrowser(down.getLocalFile());
                    switch (la) {
                        case DEFAULT:
                            engine.getPopupHandler().launchPopup(down.getLocalFile().toURI().toString());
                            break;
                        case CUSTOM:
                            break;
                    }
                }
            });
        }

        @Override
        protected void updateItem(Download item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                down = item;
                name.setText(item.getLocalFile().getAbsolutePath());
                setGraphic(box);
            }
            if (empty) {
                setGraphic(null);
            }
        }

    }
}
