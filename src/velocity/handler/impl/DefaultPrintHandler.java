/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import javafx.print.PrinterJob;
import velocity.core.VelocityEngine;
import velocity.handler.PrintHandler;
import velocity.handler.PrintStatus;

/**
 *
 * @author Aniket
 */
public class DefaultPrintHandler extends DefaultHandler implements PrintHandler {

    public DefaultPrintHandler(VelocityEngine engine) {
        super(engine);
    }

    @Override
    public PrintStatus onPrint(PrinterJob job) {
        return PrintStatus.CANCEL;
    }

}
