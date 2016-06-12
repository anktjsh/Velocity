/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.view;

/**
 *
 * @author Aniket
 */
import application.velocity.BrowserPane;
import application.velocity.BrowserView;
import application.velocity.Desktop;
import java.util.Iterator;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class TabPaneDetacher {

    private BrowserView currentTab;

    private TabPaneDetacher() {
    }

    public static TabPaneDetacher create() {
        return new TabPaneDetacher();
    }

    public TabPaneDetacher makeTabsDetachable(TabPane tabPane) {
        tabPane.setOnDragDetected(
                (MouseEvent event) -> {
                    if (event.getSource() instanceof TabPane) {
                        Pane rootPane = (Pane) tabPane.getScene().getRoot();
                        rootPane.setOnDragOver((DragEvent event1) -> {
                            event1.acceptTransferModes(TransferMode.ANY);
                            event1.consume();
                        });
                        currentTab = (BrowserView) tabPane.getSelectionModel().getSelectedItem();
                        if (currentTab instanceof BrowserView) {
                            if (tabPane.getTabs().size() > 2) {
                                BrowserView bv = (BrowserView) currentTab;
                                SnapshotParameters snapshotParams = new SnapshotParameters();
                                snapshotParams.setTransform(Transform.scale(0.4, 0.4));
                                WritableImage snapshot = currentTab.getContent().snapshot(snapshotParams, null);
                                Dragboard db = tabPane.startDragAndDrop(TransferMode.ANY);
                                ClipboardContent clipboardContent = new ClipboardContent();
                                clipboardContent.put(DataFormat.URL, bv.getVelocityEngine().getLocation());
                                db.setDragView(snapshot, 40, 40);
                                db.setContent(clipboardContent);
                            }
                        }
                    }
                    event.consume();
                }
        );
        tabPane.setOnDragDone(
                (DragEvent event) -> {
                    openTabInStage(currentTab);
                    tabPane.setCursor(Cursor.DEFAULT);
                    event.consume();
                }
        );
        return this;
    }

    private void openTabInStage(final BrowserView tab) {
        Stage stage = null;
        Iterator<Window> win = Window.impl_getWindows();
        Point2D p = MouseRobot.getMousePosition();
        while (win.hasNext()) {
            Window w = win.next();
            if (w instanceof Stage) {
                Stage s = (Stage) w;
                if (!s.equals(tab.getTabPane().getScene().getWindow())) {
                    if (p.getX() >= s.getX() && p.getX() < s.getX() + s.getWidth() && p.getY() >= s.getY() && p.getY() < s.getY() + s.getHeight()) {
                        if (s.getScene().getRoot() instanceof BrowserPane) {
                            stage = s;
                        }
                    }
                }
            }
        }
        boolean isNull = false;
        if (stage == null) {
            stage = Desktop.getBrowser();
            isNull = true;
        }
        BrowserPane bp = (BrowserPane) stage.getScene().getRoot();
        if (isNull) {
            stage.setX(p.getX());
            stage.setY(p.getY());
            stage.setOnShown((WindowEvent t) -> {
                tab.getTabPane().getTabs().remove(tab);
            });
            stage.show();
            ((TabPane) bp.getCenter()).getTabs().set(0, tab);
        } else {
            tab.getTabPane().getTabs().remove(tab);
            ((TabPane) bp.getCenter()).getTabs().add(((TabPane) bp.getCenter()).getTabs().size() - 1, tab);
        }
    }

}
