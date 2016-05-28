/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.core;

import com.jaunt.UserAgent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import velocity.handler.DownloadResult;
import velocity.util.FileUtils;

/**
 *
 * @author Aniket
 */
public class Download extends Task<File> {

    private final static Logger log = Logger.getLogger(Download.class.getName());

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private final String remoteUrl;
    private final File localFile;
    private final int bufferSize;
    private final int type;
    private final Document document;

    public Download(String remoteUrl, File localFile, Document doc, int type) {
        this.remoteUrl = remoteUrl;
        this.localFile = localFile;
        this.bufferSize = DEFAULT_BUFFER_SIZE;
        document = doc;
        this.type = type;

        stateProperty().addListener((source, oldState, newState) -> {
            if (newState.equals(Worker.State.SUCCEEDED)) {
                onSuccess();
            } else if (newState.equals(Worker.State.FAILED)) {
                onFailed();
            }
        });
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public File getLocalFile() {
        return localFile;
    }

    private InputStream getStream(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return (httpConn.getInputStream());
            }
        } catch (MalformedURLException ex) {
        } catch (IOException ex) {
        }
        return null;
    }

    @Override
    protected File call() throws Exception {
        try {
            if (type == DownloadResult.CUSTOM) {
                log.info(String.format("Downloading file %s to %s", remoteUrl, localFile.getAbsolutePath()));
                InputStream stream = getStream(remoteUrl);
                if (stream != null) {
                    File dir = localFile.getParentFile();
                    dir.mkdirs();
                    FileUtils.copy(stream, new FileOutputStream(localFile));
                    log.info(String.format("Downloading of file %s to %s completed successfully", remoteUrl, localFile.getAbsolutePath()));
                    return localFile;
                } else {
                    throw new RuntimeException("NULL HTTP CONNECTION");
                }
            } else if (type == DownloadResult.HTML) {
                StringWriter writer = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.METHOD, "html");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.transform(new DOMSource(document),
                        new StreamResult(writer));
                File dir = localFile.getParentFile();
                dir.mkdirs();
                ArrayList<String> al = new ArrayList<>();
                al.add("<!doctype html>");
                al.add(writer.getBuffer().toString());
                FileUtils.write(localFile, al);
                return localFile;
            } else if (type == DownloadResult.COMPLETE) {
                UserAgent us = new UserAgent();
                us.visit(remoteUrl);
                us.doc.saveCompleteWebPage(localFile);
                return localFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Download) {
            Download d = (Download) o;
            if (d.getLocalFile().equals(getLocalFile())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.remoteUrl);
        hash = 37 * hash + Objects.hashCode(this.localFile);
        hash = 37 * hash + this.bufferSize;
        return hash;
    }

    private void onFailed() {
        updateProgress(-1, 1);
    }

    private void onSuccess() {
        updateProgress(1, 1);
    }
}
