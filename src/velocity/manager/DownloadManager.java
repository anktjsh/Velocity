/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.manager;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import velocity.core.Download;
import velocity.util.FileUtils;

/**
 *
 * @author Aniket
 */
public class DownloadManager {

    private static final ObservableList<Download> downloads = FXCollections.observableArrayList();
    private DownloadListener down;

    private DownloadManager() {
    }

    public ObservableList<Download> getDownloads() {
        return downloads;
    }

    public void clear() {
        downloads.clear();
    }

    public void addDownload(Download d) {
        downloads.add(d);
        if (down != null) {
            down.downloadAdded(d);
        }
    }

    public static DownloadManager getInstance() {
        return DownloadManagerHolder.INSTANCE;
    }

    private static class DownloadManagerHolder {

        private static final DownloadManager INSTANCE = new DownloadManager();
    }

    public void setDownloadListener(DownloadListener dl) {
        down = dl;
    }

    public DownloadListener getDownloadListener() {
        return down;
    }

    public interface DownloadListener {

        public void downloadAdded(Download d);
    }

    public void save() {
        ArrayList<String> al = new ArrayList<>();
        for (Download we : downloads) {
            al.add(we.getRemoteUrl() + "," + we.getLocalFile().getAbsolutePath());
        }
        FileUtils.write(FileUtils.newFile("downloads.txt"), al);
    }

    public void load() {
        ArrayList<String> al = new ArrayList<>();
        al.addAll(FileUtils.readAllLines(FileUtils.newFile("downloads.txt")));
        for (String s : al) {
            String[] spl = s.split(",");
            if (spl.length == 2) {
                downloads.add(new Download(spl[0], FileUtils.newFile(spl[1]), null, -1));
            }
        }
    }
}
