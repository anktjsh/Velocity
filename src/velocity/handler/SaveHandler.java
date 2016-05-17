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
public interface SaveHandler {

    public DownloadResult automaticDownload(String url, String contentType, String filename);

    public DownloadResult saveAs(String url);

    public DownloadResult downloadImage(String url);

}
