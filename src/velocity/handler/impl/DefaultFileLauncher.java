/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler.impl;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import velocity.handler.FileLauncher;

/**
 *
 * @author Aniket
 */
public class DefaultFileLauncher implements FileLauncher {

    @Override
    public void launchFile(File f) {
        try {
            Desktop.getDesktop().open(f);
        } catch (IOException ex) {
        }
    }

}
