/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.view;

import java.util.ArrayList;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import velocity.core.VelocityEngine;
import velocity.manager.HistoryManager;
import velocity.manager.HistoryManager.WebEntry;
import velocity.util.DialogUtils;

/**
 *
 * @author Aniket
 */
public class HistoryPane extends BorderPane {

    private final ListView<WebEntry> view;
    private final VelocityEngine engine;
    private final ArrayList<WebEntry> allEntries;
    private final BorderPane top;
    private final ToolBar searchBar;
    private final Button search, clear;
    private final TextField field;

    public HistoryPane(VelocityEngine engine) {
        this.engine = engine;
        setPadding(new Insets(5, 10, 5, 10));
        view = new ListView<>();
        setCenter(view);
        setTop(top = new BorderPane());
        Label l;
        top.setTop(l = new Label("History"));
        l.setFont(new Font(18));
        top.setRight(searchBar = new ToolBar());
        searchBar.getItems().add(clear = new Button("Clear History"));
        searchBar.getItems().addAll(field = new TextField(), search = new Button("Search"));
        field.setPromptText("Search");
        allEntries = new ArrayList<>();
        field.setOnAction((e) -> {
            view.getItems().clear();
            if (!field.getText().isEmpty()) {
                for (WebEntry we : allEntries) {
                    if (we.getLocation().contains(field.getText()) || we.getTitle().contains(field.getText())) {
                        view.getItems().add(we);
                    }
                }
            } else {
                view.getItems().addAll(allEntries);
            }
        });
        search.setOnAction(field.getOnAction());
        view.setCellFactory((param) -> new HistoryCell());
        for (int x = HistoryManager.getInstance().getEntries().size() - 1; x >= 0; x--) {
            allEntries.add(HistoryManager.getInstance().getEntries().get(x));
        }
        view.getItems().addAll(allEntries);
        clear.setOnAction((e) -> {
            Optional<ButtonType> show = DialogUtils.showAlert(Alert.AlertType.CONFIRMATION, getScene() == null ? null : getScene().getWindow(), "Confirm", "Are you sure you want to clear these entries?", "");
            if (show.isPresent()) {
                if (show.get() == ButtonType.OK) {
                    HistoryManager.getInstance().clear();
                    view.getItems().clear();
                }
            }
        });
    }

    private class HistoryCell extends ListCell<WebEntry> {

        private final Hyperlink link;
        private final Label date, time, label;
        private final HBox box;

        public HistoryCell() {
            box = new HBox(5);
            link = new Hyperlink();
            link.setOnAction((e) -> {
                engine.launchPopup(link.getText());
            });
            setGraphic(box);
            label = new Label("");
            date = new Label("");
            time = new Label("");
            box.getChildren().addAll(date, time, label, link);
        }

        @Override
        protected void updateItem(WebEntry item, boolean empty) {
            if (item != null) {
                date.setText(item.getLastVisitedDate().toString());
                time.setText(item.getTimeVisited().toString());
                label.setText(item.getTitle());
                link.setText(item.getLocation());
            }
        }

    }

}
