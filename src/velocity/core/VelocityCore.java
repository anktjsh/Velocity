/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.core;

import velocity.cookie.CookieManager;
import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import velocity.handler.CertificateHandler;
import velocity.handler.FileLauncher;
import velocity.handler.impl.DefaultCertificateHandler;
import velocity.handler.impl.DefaultFileLauncher;

/**
 *
 * @author Aniket
 */
public class VelocityCore {

    private static final ObjectProperty<CertificateHandler> certificateHandler = new SimpleObjectProperty<>();
    private static final ObjectProperty<FileLauncher> fileLauncher = new SimpleObjectProperty<>();
    private static final ObjectProperty<String> defaultUserAgent = new SimpleObjectProperty<>();
    private static final ObjectProperty<String> defaultDownloadsLocation = new SimpleObjectProperty<>();
    private static final ArrayList<String> supportedFormats = new ArrayList<>();

    static {
        initialize();
        CookieHandler.setDefault(new CookieManager());        
    }

    private static void initialize() {
        TrustManager trm = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String string) throws CertificateException {
                if (getCertificateHandler() != null) {
                    for (X509Certificate c : certs) {
                        if (!getCertificateHandler().authorizeCredentials(c)) {
                            throw new CertificateException();
                        }
                    }
                }
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                if (getCertificateHandler() != null) {
                    for (X509Certificate c : certs) {
                        if (!getCertificateHandler().authorizeCredentials(c)) {
                            throw new CertificateException();
                        }
                    }
                }

            }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{trm}, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
        }
        setCertificateHandler(new DefaultCertificateHandler());
        setFileLauncher(new DefaultFileLauncher());
        defaultDownloadsLocation.set(System.getProperty("user.home") + File.separator + "Downloads");
        supportedFormats.add(".pdf");
    }

    public static void setDefaultUserAgent(String s) {
        if (s != null) {
            defaultUserAgent.set(s);
        }
    }

    public static String getDefaultUserAgent() {
        return defaultUserAgent.get();
    }

    public static CertificateHandler getCertificateHandler() {
        return certificateHandler.get();
    }

    public static void setCertificateHandler(CertificateHandler handl) {
        if (handl != null) {
            certificateHandler.set(handl);
        }
    }

    public static String getDefaultDownloadsLocation() {
        return defaultDownloadsLocation.get();
    }

    public static void setDefaultDownloadsLocation(String s) {
        if (s != null) {
            defaultDownloadsLocation.set(s);
        }
    }

    public static FileLaunchStatus launchFileInBrowser(File f) {
        String mime = null;
        try {
            mime = Files.probeContentType(f.toPath());
        } catch (IOException ex) {
        }
        for (String s : supportedFormats) {
            if (f.getAbsolutePath().endsWith(s)) {
                return FileLaunchStatus.DEFAULT;
            }
        }
        if (mime != null) {
            if (mime.startsWith("application/")) {
                if (launchFile(f)) {
                    return FileLaunchStatus.SUCCESS;
                }
            } else if (mime.startsWith("image/") || mime.startsWith("text/")) {
                return FileLaunchStatus.DEFAULT;
            } else if (launchFile(f)) {
                return FileLaunchStatus.SUCCESS;
            }
        } else if (launchFile(f)) {
            return FileLaunchStatus.SUCCESS;
        }
        return FileLaunchStatus.CUSTOM;
    }

    private static boolean launchFile(File f) {
        if (fileLauncher.get() != null) {
            fileLauncher.get().launchFile(f);
            return true;
        }
        return false;
    }

    public static FileLauncher getFileLauncher() {
        return fileLauncher.get();
    }

    public static void setFileLauncher(FileLauncher handl) {
        if (handl != null) {
            fileLauncher.set(handl);
        }
    }

    public enum FileLaunchStatus {
        DEFAULT, CUSTOM, SUCCESS;
        //SUCCESS, FAILURE
    }
}
