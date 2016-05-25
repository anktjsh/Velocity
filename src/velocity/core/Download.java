/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.util.Pair;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
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
    private final NodeList nodeList;

    public Download(String remoteUrl, File localFile, Document doc, int type) {
        this.remoteUrl = remoteUrl;
        this.localFile = localFile;
        this.bufferSize = DEFAULT_BUFFER_SIZE;
        document = doc;
        this.type = type;
        nodeList = doc == null ? null : doc.getElementsByTagName("*");

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

    private Pair<InputStream, List<String>> getStream(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return new Pair<>(httpConn.getInputStream(), httpConn.getHeaderFields().get("Content-Disposition"));
            }
        } catch (MalformedURLException ex) {
        } catch (IOException ex) {
        }
        return null;
    }

    private Pair<InputStream, List<String>> getStream(String parent, String relative) {
        try {
            URL u = new URL(relative.contains(":") ? relative : "https:" + relative);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return new Pair<>(httpConn.getInputStream(), httpConn.getHeaderFields().get("Content-Disposition"));
            }
        } catch (Exception ex) {
        }
        try {
            URL u = new URL(parent + relative);
            HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return new Pair<>(httpConn.getInputStream(), httpConn.getHeaderFields().get("Content-Disposition"));
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    protected File call() throws Exception {
        try {
            if (type == DownloadResult.CUSTOM) {
                log.info(String.format("Downloading file %s to %s", remoteUrl, localFile.getAbsolutePath()));
                Pair<InputStream, List<String>> par = getStream(remoteUrl);
                if (par != null) {
                    InputStream stream = getStream(remoteUrl).getKey();
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
                File dir = localFile.getParentFile();
                dir.mkdirs();
                File files = new File(dir.getAbsolutePath() + File.separator + "files");
                files.mkdir();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (((Element) nodeList.item(i)).getAttribute("src") != null) {
                        Pair<InputStream, List<String>> par = getStream(remoteUrl, ((Element) nodeList.item(i)).getAttribute("src"));
                        if (par != null) {
                            InputStream stream = par.getKey();
                            String filename = getFileName(par.getValue());
                            if (filename == null) {
                                if (((Element) nodeList.item(i)).getTagName().equalsIgnoreCase("script")) {
                                    filename = "script" + i + ".js";
                                } else {
                                    String src = ((Element) nodeList.item(i)).getAttribute("src");
                                    if (src.contains("/")) {
                                        filename = src.substring(src.lastIndexOf("/") + 1);
                                    } else {
                                        filename = src;
                                    }
                                }
                            }
                            if (filename != null) {
                                File temp = new File(files.getAbsolutePath() + File.separator + filename);
                                FileUtils.copy(stream, new FileOutputStream(temp));
                                ((Element) nodeList.item(i)).setAttribute("src", "files/" + temp.getName());
                                if (((Element) nodeList.item(i)).getAttribute("srcset") != null) {
                                    ((Element) nodeList.item(i)).setAttribute("srcset", "files/" + temp.getName());
                                }
                            }
                        } else {
//                            System.out.println(((Element) nodeList.item(i)).getAttribute("src"));
                        }
                    }
                }
                StringWriter writer = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.METHOD, "html");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.transform(new DOMSource(document),
                        new StreamResult(writer));
                ArrayList<String> al = new ArrayList<>();
                al.add("<!doctype html>");
                al.add(writer.getBuffer().toString());
                FileUtils.write(localFile, al);

                return localFile;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }

    private String getFileName(List<String> disposition) {
        if (disposition != null) {
            String one = disposition.get(0);
            if (one.contains("filename*=UTF-8''")) {
                String temp = one.substring(one.indexOf("filename*=UTF-8''") + "filename*=UTF-8''".length());
                return temp;
            } else if (one.contains("filename=\"")) {
                String temp = one.substring(one.indexOf("filename=\"") + "filename=\"".length());
                return temp.substring(0, temp.indexOf("\""));
            }
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
