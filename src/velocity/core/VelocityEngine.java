/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.core;

import com.sun.javafx.scene.control.skin.ContextMenuContent;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import velocity.handler.AlertHandler;
import velocity.handler.ConfirmHandler;
import velocity.handler.ContextMenuHandler;
import velocity.handler.ContextMenuParams;
import velocity.handler.DownloadResult;
import velocity.handler.ErrorHandler;
import velocity.handler.LoadListener;
import velocity.handler.PopupHandler;
import velocity.handler.PrintHandler;
import velocity.handler.PrintStatus;
import velocity.handler.ProgressListener;
import velocity.handler.PromptHandler;
import velocity.handler.SaveHandler;
import velocity.handler.StatusListener;
import velocity.handler.ViewSourceHandler;
import velocity.handler.WebAlert;
import velocity.handler.impl.DefaultAlertHandler;
import velocity.handler.impl.DefaultConfirmHandler;
import velocity.handler.impl.DefaultContextMenuHandler;
import velocity.handler.impl.DefaultErrorHandler;
import velocity.handler.impl.DefaultPopupHandler;
import velocity.handler.impl.DefaultPrintHandler;
import velocity.handler.impl.DefaultPromptHandler;
import velocity.handler.impl.DefaultSaveHandler;
import velocity.handler.impl.DefaultViewSourceHandler;
import velocity.manager.DownloadManager;
import velocity.manager.HistoryManager;
import velocity.plugin.Plugin;
import velocity.util.FileUtils;
import velocity.view.Viewer;

/**
 *
 * @author Aniket
 */
public final class VelocityEngine {

    final WebView web;

    private final VelocityView view;
    private final ObjectProperty<String> locationProperty = new SimpleObjectProperty<>("");
    private final ObjectProperty<String> titleProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ContextMenuHandler> contextMenuHandler = new SimpleObjectProperty<>();
    private final ObjectProperty<PrintHandler> printHandler = new SimpleObjectProperty<>();
    private final ObjectProperty<ProgressListener> progressListener = new SimpleObjectProperty<>();
    private final ObjectProperty<ViewSourceHandler> viewSourceHandler = new SimpleObjectProperty<>();
    private final ObjectProperty<AlertHandler> alertHandler = new SimpleObjectProperty<>();
    private final ObjectProperty<SaveHandler> saveHandler = new SimpleObjectProperty<>();
    private final ObjectProperty<PopupHandler> popupHandler = new SimpleObjectProperty<>();
    private final ObjectProperty<ConfirmHandler> confirmHandler = new SimpleObjectProperty<>();
    private final ObjectProperty<StatusListener> statusListener = new SimpleObjectProperty<>();
    private final ObjectProperty<LoadListener> loadListener = new SimpleObjectProperty<>();
    private final ObjectProperty<PromptHandler> promptHandler = new SimpleObjectProperty<>();
    private final ObjectProperty<ErrorHandler> errorHandler = new SimpleObjectProperty<>();
    private final String defaultUserAgent;
    private final ReadOnlyBooleanWrapper canGoForwardProperty = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canGoBackProperty = new ReadOnlyBooleanWrapper();
    private final BooleanProperty dialogsSuppressed = new SimpleBooleanProperty();
    private final BooleanProperty popupsSuppressed = new SimpleBooleanProperty();
    private final BooleanProperty incognitoProperty = new SimpleBooleanProperty();
    private String htmlText = "";

    private boolean isImage;
    private boolean isLink;
    private String srcUrl;
    private String linkUrl;
    private String selectedText;
    private String linkText;

    private final History history;

    VelocityEngine(VelocityView v) {
        view = v;
        web = new WebView();
//        try {
//            DevToolsDebuggerServer.startDebugServer(web.getEngine().impl_getDebugger(), 51742);
//        } catch (Exception ex) {
//            Logger.getLogger(VelocityEngine.class.getName()).log(Level.SEVERE, null, ex);
//        }
        if (VelocityCore.isDesktop()) {
            defaultUserAgent = web.getEngine().getUserAgent();
            if (VelocityCore.getDefaultUserAgent() != null) {
                web.getEngine().setUserAgent(VelocityCore.getDefaultUserAgent());
            }
        } else {
            defaultUserAgent = "";
        }
        history = new History(this);
        web.setOnDragOver((event) -> {
            event.consume();
        });
        web.setOnContextMenuRequested((e) -> {
            if (VelocityCore.isDesktop()) {
                ContextMenuParams params = new ContextMenuParams(this,
                        getContextMenu(),
                        linkUrl,
                        linkText,
                        srcUrl,
                        web.getEngine().getLocation(),
                        selectedText,
                        isImage,
                        isLink);
                if (getContextMenuHandler() != null) {
                    getContextMenuHandler().showContextMenu(params);
                }
                isImage = false;
                isLink = false;
                srcUrl = "";
                linkUrl = "";
                selectedText = "";
                linkText = "";
            }
        });
        HistoryManager.getInstance().addEngine(VelocityEngine.this);
        locationProperty.addListener((ob, older, newer) -> {
            if (newer != null) {
                Plugin plugin = null;
                boolean supported = false;
                if (htmlText.isEmpty() && (plugin = VelocityCore.getUrlPlugin(newer, this)) != null) {
                    Pair<Node, String> pa;
                    view.setCenter((pa = plugin.getNodeAndTitle(this, newer)).getKey());
                    titleProperty.set(pa.getValue());
                    supported = true;
                } else if (newer.startsWith("velocityfx://source-")) {
                    if (newer.contains("\t")) {
                        String[] spl = newer.split("\t");
                        titleProperty.set("View Source");
                        view.setCenter(new Viewer(spl[2]));
                        locationProperty.set(spl[0] + spl[1]);
                    }
                    supported = true;
                }
                String contentType = getContentType(newer);
                List<String> disposition = getContentDisposition(newer);
                if ((contentType != null && !contentType.isEmpty())
                        && !(contentType.startsWith("text/")) && !contentType.startsWith("image/")
                        && (!newer.isEmpty() && !newer.equals("about:blank"))
                        && getSaveHandler() != null && !supported) {
                    String filename = getFileName(disposition);
                    DownloadResult f = getSaveHandler().automaticDownload(newer, contentType, filename);
                    if (f != null) {
                        download(newer, f.getFile(), f.getType());
                    }
                } else if (htmlText.isEmpty() && FileUtils.isFile(newer) && (plugin = VelocityCore.getFilePlugin(newer, this)) != null) {
                    Pair<Node, String> pa;
                    view.setCenter((pa = plugin.getNodeAndTitle(this, newer)).getKey());
                    titleProperty.set(pa.getValue());
                } else if (!(view.getCenter() instanceof WebView) && !supported) {
                    view.setCenter(web);
                }
                htmlText = "";
            }
        });
        if (VelocityCore.isDesktop()) {
            web.getEngine().documentProperty().addListener((ob, older, newer) -> {
                if (newer != null) {
                    registerListeners(newer);
                }
            });
        }
        web.getEngine().titleProperty().addListener((ob, older, newer) -> {
            titleProperty.set(newer);
        });
        if (VelocityCore.isDesktop()) {
            web.getEngine().getHistory().currentIndexProperty().addListener((ob, older, newer) -> {
                String url = web.getEngine().getHistory().getEntries().get(newer.intValue()).getUrl();
                if (!web.getEngine().getLocation().equals(url)) {
                    locationProperty.set(url);
                    canGoBackProperty.set(internalCanGoBack());
                    canGoForwardProperty.set(internalCanGoForward());
                    history.add(url, web.getEngine().getTitle());
                }
            });
        }
        getHistory().currentLocationProperty().addListener((ob, older, newer) -> {
            if (!locationProperty.get().equals(newer.getLocation())) {
                locationProperty.set(newer.getLocation());
            }
            boolean canGoBack = internalCanGoBack();
            boolean canGoForward = internalCanGoForward();
            canGoBackProperty.set(canGoBack);
            canGoForwardProperty.set(canGoForward);
        });
        web.getEngine().locationProperty().addListener((o, older, newer) -> {
            if (!locationProperty.get().equals(newer)) {
                locationProperty.set(newer);
            }
            boolean canGoBack = internalCanGoBack();
            boolean canGoForward = internalCanGoForward();
            canGoBackProperty.set(canGoBack);
            canGoForwardProperty.set(canGoForward);
        });
        web.getEngine().getLoadWorker().progressProperty().addListener((ob, older, newer) -> {
            if (newer != null) {
                if (getProgressListener() != null) {
                    getProgressListener().onProgress(newer.doubleValue());
                }
            }
        });
        web.getEngine().getLoadWorker().stateProperty().addListener((ob, older, newer) -> {
            if (getLoadListener() != null) {
                switch (newer) {
                    case SCHEDULED:
                        getLoadListener().onLoadScheduled();
                        break;
                    case SUCCEEDED:
                        getLoadListener().onLoadCompleted();
                        break;
                    case CANCELLED:
                        getLoadListener().onLoadCancelled();
                        break;
                    case READY:
                        getLoadListener().onLoadReady();
                        break;
                    case FAILED:
                        getLoadListener().onLoadFailed();
                        break;
                    case RUNNING:
                        getLoadListener().onLoadRunning();
                        break;
                }
            }
        });
        web.getEngine().setOnAlert((e) -> {
            if (!dialogsSuppressed()) {
                if (getAlertHandler() != null) {
                    getAlertHandler().handle(new WebAlert(this, e));
                }
            }
        });
        web.getEngine().setCreatePopupHandler((param) -> {
            if (!popupsSuppressed()) {
                if (getPopupHandler() != null) {
                    return getPopupHandler().newTab(param).web.getEngine();
                }
            } else {
                VelocityView vv = new VelocityView();
                vv.getEngine().locationProperty.addListener((ob, older, newer) -> {
                    if (!newer.equals("about:blank")) {
                        VelocityCore.getBlockedUrls().add(newer);
                        (new Thread(() -> {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                            Platform.runLater(() -> {
                                vv.dispose();
                            });
                        })).start();
                    }
                });
                return vv.getEngine().web.getEngine();
            }
            return null;
        });
        web.getEngine().setPromptHandler((param) -> {
            if (!dialogsSuppressed()) {
                if (getPromptHandler() != null) {
                    return getPromptHandler().call(param);
                }
            }
            return null;
        });
        web.getEngine().setConfirmHandler((param) -> {
            if (!dialogsSuppressed()) {
                if (getConfirmHandler() != null) {
                    return getConfirmHandler().call(param);
                }
            }
            return null;
        });
        web.getEngine().setOnStatusChanged((event) -> {
            if (getStatusListener() != null) {
                getStatusListener().statusChanged(event.getData());
            }
        });
        if (VelocityCore.isDesktop()) {
            web.getEngine().setOnError((event) -> {
                if (getErrorHandler() != null) {
                    getErrorHandler().handle(event);
                }
            });
        }
//        web.getEngine().setOnResized(null);
//        web.getEngine().setOnVisibilityChanged(null);
        setPromptHandler(new DefaultPromptHandler(this));
        setErrorHandler(new DefaultErrorHandler(this));
        setSaveHandler(new DefaultSaveHandler(this));
        setAlertHandler(new DefaultAlertHandler(this));
        setContextMenuHandler(new DefaultContextMenuHandler(this));
        setPrintHandler(new DefaultPrintHandler(this));
        setViewSourceHandler(new DefaultViewSourceHandler(this));
        setPopupHandler(new DefaultPopupHandler(this));
        setConfirmHandler(new DefaultConfirmHandler(this));
        incognitoProperty.set(false);
        incognitoProperty.addListener((ob, older, newer) -> {
            if (newer) {
                if (!older) {
                    HistoryManager.getInstance().removeEngine(this);
                }
            } else if (older) {
                HistoryManager.getInstance().removeEngine(this);
            }
        });
    }

    public boolean isIncognito() {
        return incognitoProperty.get();
    }

    public BooleanProperty incognitoProperty() {
        return incognitoProperty;
    }

    public void setIncognito(boolean b) {
        incognitoProperty.set(b);
    }

    public void launchPopupInTab(String url) {
        if (getPopupHandler() != null) {
            getPopupHandler().newTab().load(url);
        }
    }

    public void launchPopupInWindow(String url) {
        if (getPopupHandler() != null) {
            getPopupHandler().newWindow().load(url);
        }
    }

    public boolean dialogsSuppressed() {
        return dialogsSuppressed.get();
    }

    public void setDialogsSuppressed(boolean b) {
        dialogsSuppressed.set(b);
    }

    public BooleanProperty dialogsSuppressedProperty() {
        return dialogsSuppressed;
    }

    public boolean popupsSuppressed() {
        return popupsSuppressed.get();
    }

    public void setPopupsSuppressed(boolean b) {
        popupsSuppressed.set(b);
    }

    public BooleanProperty popupsSuppressedProperty() {
        return popupsSuppressed;
    }

    private String getFileName(List<String> disposition) {
        try {
            if (disposition != null) {
                String one = disposition.get(0);
                if (one.contains("filename*=UTF-8''")) {
                    String temp = one.substring(one.indexOf("filename*=UTF-8''") + "filename*=UTF-8''".length());
                    return temp;
                } else if (one.contains("filename=\"")) {
                    String temp = one.substring(one.indexOf("filename=\"") + "filename=\"".length());
                    return temp.substring(0, temp.indexOf('"'));
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public ReadOnlyBooleanProperty canGoForwardProperty() {
        return canGoForwardProperty.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty canGoBackProperty() {
        return canGoBackProperty.getReadOnlyProperty();
    }

    public Document getDocument() {
        return web.getEngine().getDocument();
    }

    public ReadOnlyObjectProperty<Document> documentProperty() {
        return web.getEngine().documentProperty();
    }

    public String getUserAgent() {
        if (VelocityCore.isDesktop()) {
            return web.getEngine().getUserAgent();
        }
        return "";
    }

    public ObjectProperty<String> titleProperty() {
        return titleProperty;
    }

    public String getTitle() {
        return titleProperty.get();
    }

    public ObjectProperty<String> locationProperty() {
        return locationProperty;
    }

    public VelocityView getVelocityView() {
        return view;
    }

    public ObjectProperty<ContextMenuHandler> contextMenuHandlerProperty() {
        return contextMenuHandler;
    }

    public void setContextMenuHandler(ContextMenuHandler handler) {
        contextMenuHandler.set(handler);
    }

    public ContextMenuHandler getContextMenuHandler() {
        return contextMenuHandler.get();
    }

    public void setUserAgent(String useragent) {
        if (VelocityCore.isDesktop()) {
            web.getEngine().setUserAgent(useragent);
        }
    }

    public void restoreUserAgent() {
        if (VelocityCore.isDesktop()) {
            web.getEngine().setUserAgent(defaultUserAgent);
        }
    }

    public void enableJavaScript() {
        if (VelocityCore.isDesktop()) {
            web.getEngine().setJavaScriptEnabled(true);
        }
    }

    public void disableJavascript() {
        if (VelocityCore.isDesktop()) {
            web.getEngine().setJavaScriptEnabled(false);
        }
    }

    public boolean isJavaScriptEnabled() {
        if (VelocityCore.isDesktop()) {
            return web.getEngine().isJavaScriptEnabled();
        }
        return false;
    }

    public String getLocation() {
        return locationProperty.get();
    }

    public History getHistory() {
        return history;
    }

    public void refreshPage() {
        if (VelocityCore.isDesktop()) {
            web.getEngine().reload();
        } else {
            web.getEngine().load(web.getEngine().getLocation());
        }
    }

    public void stopLoad() {
        web.getEngine().getLoadWorker().cancel();
    }

    private void registerListeners(Document doc) {
        EventListener listener = (Event evt) -> {
            String domEventType = evt.getType();
            if (domEventType.equals("contextmenu")) {
                String tagName = ((Element) evt.getTarget()).getTagName();
                if (tagName.equalsIgnoreCase("img") || tagName.equalsIgnoreCase("a") || tagName.equalsIgnoreCase("area")) {
                    isImage = tagName.equalsIgnoreCase("img");
                    isLink = ((Element) evt.getTarget()).hasAttribute("href");
                    linkUrl = ((Element) evt.getTarget()).getAttribute("href");
                    srcUrl = ((Element) evt.getTarget()).getAttribute("src");
                    selectedText = (String) web.getEngine()
                            .executeScript("window.getSelection().toString()");
                    String id = ((Element) evt.getTarget()).getAttribute("id");
                    linkText = (id == null) ? null : (String) web.getEngine()
                            .executeScript("document.getElementById('" + id + "').innerHTML");
                } else if (((Element) evt.getTarget()).getParentNode() instanceof Element) {
                    String tag = ((Element) ((Element) evt.getTarget()).getParentNode()).getTagName();
                    if (tag.equalsIgnoreCase("a") || tag.equalsIgnoreCase("area")) {
                        isImage = tag.equalsIgnoreCase("img");
                        isLink = ((Element) ((Element) evt.getTarget()).getParentNode()).hasAttribute("href");
                        linkUrl = ((Element) ((Element) evt.getTarget()).getParentNode()).getAttribute("href");
                        srcUrl = ((Element) ((Element) evt.getTarget()).getParentNode()).getAttribute("src");
                        selectedText = (String) web.getEngine()
                                .executeScript("window.getSelection().toString()");
                        String id = ((Element) ((Element) evt.getTarget()).getParentNode()).getAttribute("id");
                        linkText = (id == null) ? null : (String) web.getEngine()
                                .executeScript("document.getElementById('" + id + "').innerHTML");
                    }
                }
            }
        };
        NodeList nodeList = doc.getElementsByTagName("img");
        for (int i = 0; i < nodeList.getLength(); i++) {
            ((EventTarget) nodeList.item(i)).addEventListener("contextmenu", listener, false);
        }
        nodeList = doc.getElementsByTagName("a");
        for (int i = 0; i < nodeList.getLength(); i++) {
            ((EventTarget) nodeList.item(i)).addEventListener("contextmenu", listener, false);
        }
        nodeList = doc.getElementsByTagName("area");
        for (int i = 0; i < nodeList.getLength(); i++) {
            ((EventTarget) nodeList.item(i)).addEventListener("contextmenu", listener, false);
        }
    }

    private ContextMenuContent getContextMenu() {
        final Iterator<Window> windows = Window.impl_getWindows();
        while (windows.hasNext()) {
            final Window window = windows.next();
            if (window instanceof ContextMenu) {
                if (window.getScene() != null && window.getScene().getRoot() != null) {
                    Parent root = window.getScene().getRoot();
                    if (root.getChildrenUnmodifiable().size() > 0) {
                        Node popup = root.getChildrenUnmodifiable().get(0);
                        if (popup.lookup(".context-menu") != null) {
                            Node bridge = popup.lookup(".context-menu");
                            ContextMenuContent cmc = (ContextMenuContent) ((Parent) bridge).getChildrenUnmodifiable().get(0);
                            return cmc;
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }

    public void goBack() {
        if (internalCanGoBack()) {
            history.go(-1);
        }
    }

    public void goForward() {
        if (internalCanGoForward()) {
            history.go(1);
        }
    }

    private boolean internalCanGoBack() {
        return getHistory().canGoBack();
    }

    private boolean internalCanGoForward() {
        return getHistory().canGoForward();
    }

    public boolean canGoBack() {
        return canGoBackProperty.get();
    }

    public boolean canGoForward() {
        return canGoForwardProperty.get();
    }

    public void print() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (getPrintHandler() != null) {
            PrintStatus st = getPrintHandler().onPrint(job);
            if (!st.isCancel()) {
                web.getEngine().print(job);
                job.endJob();
            }
        }
    }

    public ObjectProperty<PrintHandler> printHandlerProperty() {
        return printHandler;
    }

    public void setPrintHandler(PrintHandler handler) {
        printHandlerProperty().set(handler);
    }

    public PrintHandler getPrintHandler() {
        return printHandlerProperty().get();
    }

    public Object executeScript(String s) {
        return web.getEngine().executeScript(s);
    }

    public final void load(String url) {
        if (url.startsWith("velocityfx://")) {
            web.getEngine().load("");
            locationProperty.set(url);
        } else {
            web.getEngine().load(url);
        }
    }

    public ObjectProperty<ViewSourceHandler> viewSourceHandlerProperty() {
        return viewSourceHandler;
    }

    public void setViewSourceHandler(ViewSourceHandler handler) {
        viewSourceHandlerProperty().set(handler);
    }

    public ViewSourceHandler getViewSourceHandler() {
        return viewSourceHandlerProperty().get();
    }

    public ObjectProperty<ProgressListener> progressListenerProperty() {
        return progressListener;
    }

    public void setProgressListener(ProgressListener handler) {
        progressListenerProperty().set(handler);
    }

    public ProgressListener getProgressListener() {
        return progressListenerProperty().get();
    }

    public ObjectProperty<AlertHandler> alertHandlerProperty() {
        return alertHandler;
    }

    public void setAlertHandler(AlertHandler handler) {
        alertHandlerProperty().set(handler);
    }

    public AlertHandler getAlertHandler() {
        return alertHandlerProperty().get();
    }

    public void saveImage(String url) {
        if (getSaveHandler() != null) {
            DownloadResult f = getSaveHandler().downloadImage(url);
            if (f != null) {
                download(url, f.getFile(), f.getType());
            }
        }
    }

    public void saveAs(String url) {
        if (getSaveHandler() != null) {
            DownloadResult f = getSaveHandler().saveAs(url);
            if (f != null) {
                download(url, f.getFile(), f.getType());
            }
        }
    }

    public void setSaveHandler(SaveHandler save) {
        saveHandler.set(save);
    }

    public ObjectProperty<SaveHandler> saveHandlerProperty() {
        return saveHandler;
    }

    public SaveHandler getSaveHandler() {
        return saveHandlerProperty().get();
    }

    public void setPopupHandler(PopupHandler save) {
        popupHandler.set(save);
    }

    public ObjectProperty<PopupHandler> popupHandlerProperty() {
        return popupHandler;
    }

    public PopupHandler getPopupHandler() {
        return popupHandlerProperty().get();
    }

    public void setConfirmHandler(ConfirmHandler save) {
        confirmHandler.set(save);
    }

    public ObjectProperty<ConfirmHandler> confirmHandlerProperty() {
        return confirmHandler;
    }

    public ConfirmHandler getConfirmHandler() {
        return confirmHandlerProperty().get();
    }

    public void setStatusListener(StatusListener sl) {
        statusListener.set(sl);
    }

    public StatusListener getStatusListener() {
        return statusListener.get();
    }

    public ObjectProperty<StatusListener> statusListenerProperty() {
        return statusListener;
    }

    public void setLoadListener(LoadListener sl) {
        loadListener.set(sl);
    }

    public LoadListener getLoadListener() {
        return loadListener.get();
    }

    public ObjectProperty<LoadListener> loadListenerProperty() {
        return loadListener;
    }

    public void setPromptHandler(PromptHandler sl) {
        promptHandler.set(sl);
    }

    public PromptHandler getPromptHandler() {
        return promptHandler.get();
    }

    public ObjectProperty<PromptHandler> promptHandlerProperty() {
        return promptHandler;
    }

    public void setErrorHandler(ErrorHandler sl) {
        errorHandler.set(sl);
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler.get();
    }

    public ObjectProperty<ErrorHandler> errorHandlerProperty() {
        return errorHandler;
    }

    private void download(String url, File file, int type) {
        if (file != null) {
            Download d = new Download(url, file, getDocument(), type);
            DownloadManager.getInstance().addDownload(d);
            (new Thread(d)).start();
        }
    }

    void dispose() {
        stopLoad();
        load("");
        history.dispose();
//        try {
//            DevToolsDebuggerServer.stopDebugServer();
//        } catch (Exception ex) {
//            Logger.getLogger(VelocityEngine.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private String getContentType(String url) {
        try {
            URL urlas = new URL(url);
            URLConnection conn = urlas.openConnection();
            return conn.getHeaderFields().get("Content-Type").get(0);
        } catch (Exception e) {
        }
        return "";
    }

    private List<String> getContentDisposition(String url) {
        try {
            URL urlas = new URL(url);
            URLConnection conn = urlas.openConnection();
            return conn.getHeaderFields().get("Content-Disposition");
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    public void loadHtml(String html) {
        htmlText = html;
        web.getEngine().loadContent(html);
    }
}
