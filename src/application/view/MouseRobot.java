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
import com.sun.glass.ui.Application;
import com.sun.glass.ui.Robot;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

public class MouseRobot {

    private static final Robot robot = Application.GetApplication().createRobot();

    public static Point2D getMousePosition() {
        return new Point2D(robot.getMouseX(), robot.getMouseY());
    }

    public static Point2D getMouseOnScreenPosition(MouseEvent event) {
        return new Point2D(event.getScreenX(), event.getScreenY());
    }

    public static Point2D getMouseInScenePosition(MouseEvent event) {
        return new Point2D(event.getSceneX(), event.getSceneY());
    }

}
