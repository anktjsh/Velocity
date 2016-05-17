/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.core;

import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.BorderPane;

/**
 *
 * @author Aniket
 */
public class VelocityView extends BorderPane {

    private final VelocityEngine engine;

    public VelocityView() {
        engine = new VelocityEngine(this);
        setCenter(engine.web);
    }

    public VelocityEngine getEngine() {
        return engine;
    }

    public void dispose() {
        engine.dispose();
    }

    public DoubleProperty zoomProperty() {
        return engine.web.zoomProperty();
    }

    public void setZoom(double d) {
        engine.web.zoomProperty().set(d);
    }

    public double getZoom() {
        return zoomProperty().get();
    }
}
