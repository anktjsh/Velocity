/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

import javafx.print.PrinterJob;

/**
 *
 * @author Aniket
 */
public interface PrintHandler {

    public PrintStatus onPrint(PrinterJob job);
}
