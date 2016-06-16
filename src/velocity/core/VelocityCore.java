/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.core;

import com.gluonhq.charm.down.common.PlatformFactory;
import java.io.File;
import java.net.CookieHandler;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import velocity.cookie.CookieManager;
import velocity.handler.CertificateHandler;
import velocity.handler.CookieListener;
import velocity.handler.FileLauncher;
import velocity.handler.impl.DefaultCertificateHandler;
import velocity.handler.impl.DefaultFileLauncher;
import velocity.plugin.Plugin;
import velocity.util.FileUtils;

/**
 *
 * @author Aniket
 */
public class VelocityCore {

    private static final ObjectProperty<CertificateHandler> certificateHandler = new SimpleObjectProperty<>();
    private static final ObjectProperty<CookieListener> cookieListener = new SimpleObjectProperty<>();
    private static final ObjectProperty<FileLauncher> fileLauncher = new SimpleObjectProperty<>();
    private static final ObjectProperty<String> defaultUserAgent = new SimpleObjectProperty<>();
    private static final ObjectProperty<String> defaultDownloadsLocation = new SimpleObjectProperty<>();
    private static final ArrayList<String> supportedFormats = new ArrayList<>();
    private static final ObservableList<String> blocked = FXCollections.observableArrayList();
    public static String current = PlatformFactory.getPlatform().getName();
    public static String DESKTOP = PlatformFactory.DESKTOP;
    public static String ANDROID = PlatformFactory.ANDROID;
    public static String IOS = PlatformFactory.IOS;
    private static final ArrayList<Plugin> plugins = new ArrayList<>();

    public static Plugin getUrlPlugin(String url, VelocityEngine eng) {
        for (Plugin p : plugins) {
            switch (p.getFormat()) {
                case URL:
                    if (url.equals(p.getId())) {
                        return p;
                    }
                    break;
            }
        }
        return null;
    }

    public static Plugin getFilePlugin(String url, VelocityEngine eng) {
        File f = new File(url);
        for (Plugin p : plugins) {
            switch (p.getFormat()) {
                case FILE_EXTENSION:
                    if (f.getName().endsWith(p.getId())) {
                        return p;
                    }
                    break;
            }
        }
        return null;
    }

    public static void addPlugin(Plugin p) {
        plugins.add(p);
    }

    public static void removePlugin(Plugin p) {
        plugins.remove(p);
    }

    public static boolean isDesktop() {
        return current.equals(DESKTOP);
    }

    public static boolean isIOS() {
        return current.equals(IOS);
    }

    public static boolean isAndroid() {
        return current.equals(ANDROID);
    }

    static {
        init();
    }

    public static void init() {
        if (!(CookieHandler.getDefault() instanceof CookieManager)) {
            initialize();
            CookieHandler.setDefault(new CookieManager());
        }
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

    public static CookieListener getCookieListener() {
        return cookieListener.get();
    }

    public static void setCookieListener(CookieListener handl) {
        if (handl != null) {
            cookieListener.set(handl);
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
        String mime = FileUtils.probeContentType(f);
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

    public static List<String> getBlockedUrls() {
        return blocked;
    }

    public enum FileLaunchStatus {
        DEFAULT, CUSTOM, SUCCESS;
    }
}
