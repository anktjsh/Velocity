/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.handler;

/**
 *
 * @author Aniket
 */
import java.io.File;

public class DownloadResult {

    public static int HTML = 0, COMPLETE = 1, CUSTOM = 2;
    private final File file;
    private final int type;

    public DownloadResult(File f, int t) {
        file = f;
        type = t;
    }

    public File getFile() {
        return file;
    }

    public int getType() {
        return type;
    }
}
