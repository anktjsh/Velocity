/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application.features;

import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import java.text.DecimalFormat;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import velocity.core.VelocityCore;
import velocity.core.VelocityView;

/**
 *
 * @author Aniket
 */
public class ZoomMenuItem extends CustomMenuItem {

    private final Button plus, minus;
    private final Label name;
    private final HBox cont;

    public ZoomMenuItem(VelocityView web) {
        DecimalFormat df = new DecimalFormat("00");
        setContent(cont = new HBox(10, minus = VelocityCore.isDesktop() ? new Button("-") : MaterialDesignIcon.ZOOM_OUT.button(),
                name = new Label("Zoom : " + df.format(web.getZoom() * 100) + "%"),
                plus = VelocityCore.isDesktop() ? new Button("+") : MaterialDesignIcon.ZOOM_IN.button()));
        cont.setAlignment(Pos.CENTER);
        setHideOnClick(false);
        web.zoomProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                name.setText("Zoom : " + df.format(newer.doubleValue() * 100) + "%");
            }
        });
        minus.setOnAction((e) -> {
            if (web.getZoom() > 0.2) {
                web.setZoom(web.getZoom() - 0.1);
            }
        });
        plus.setOnAction((e) -> {
            if (web.getZoom() < 3.0) {
                web.setZoom(web.getZoom() + 0.1);
            }
        });
    }
}
