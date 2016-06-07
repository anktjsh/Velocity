/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.core;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import netscape.javascript.JSObject;

/**
 *
 * @author Aniket
 */
public class History {

    private Trie tree;
    private final ReadOnlyObjectWrapper<Entry> currentIndex;
    private final ObservableList<Entry> entries;
    private final VelocityEngine engine;

    public History(VelocityEngine eng) {
        engine = eng;
        currentIndex = new ReadOnlyObjectWrapper<>();
        if (VelocityCore.isDesktop()) {
            eng.documentProperty().addListener((ob, older, newer) -> {
                if (newer != null) {
                    JSObject docu = (JSObject) eng.executeScript("document");
                    String s = eng.getLocation();
                    if (s != null && !s.isEmpty() && !s.equals("about:blank")) {
                        if (tree == null) {
                            tree = new Trie(newer.getDocumentURI(), (String) docu.getMember("title"));
                            tree.currentNode.addListener((ab, older2, newer2) -> {
                                if (newer2 != null) {
                                    currentIndex.set(newer2);
                                }
                            });
                        } else {
                            tree.add(newer.getDocumentURI(), (String) docu.getMember("title"));
                        }
                    }
                }
            });
        } else {
            eng.locationProperty().addListener((ob, older, newer) -> {
                if (newer != null) {
                    String s = eng.getLocation();
                    if (s != null && !s.isEmpty() && !s.equals("about:blank")) {
                        if (!s.startsWith("velocityfx://")) {
                            if (tree == null) {
                                tree = new Trie(s, "");
                                tree.currentNode.addListener((ab, older2, newer2) -> {
                                    if (newer2 != null) {
                                        currentIndex.set(newer2);
                                    }
                                });
                            } else {
                                tree.add(s, "");
                            }
                        }
                    }
                }
            });
        }
        entries = FXCollections.observableArrayList();
    }

    public Entry getCurrentLocation() {
        return currentIndex.get();
    }

    public ReadOnlyObjectProperty<Entry> currentLocationProperty() {
        return currentIndex.getReadOnlyProperty();
    }

    public ObservableList<Entry> getEntries() {
        return entries;
    }

    public boolean canGoForward() {
        if (tree == null) {
            return false;
        }
        return tree.canGoForward();
    }

    public boolean canGoBack() {
        if (tree == null) {
            return false;
        }
        return tree.canGoBack();
    }

    public void go(int n) {
        if (n == 0) {
            return;
        }
        if (n < 0) {
            for (int x = 0; x < Math.abs(n); x++) {
                if (tree != null) {
                    tree.goBack();
                }
            }
        } else {
            for (int x = 0; x < n; x++) {
                if (tree != null) {
                    tree.goForward();
                }
            }
        }
    }

    public void dispose() {
        if (tree != null) {
            tree.destroy();
        }
    }

    void add(String s, String t) {
        tree.add(s, t);
    }

    private class Trie {

        private ObjectProperty<Entry> currentNode;
        private Entry lastNode;
        private final Entry root;

        public Trie(String url, String ti) {
            root = new Entry(null, url, ti);
            currentNode = new SimpleObjectProperty<>(root);
            entries.add(root);
        }

        public void goBack() {
            if (canGoBack()) {
                lastNode = currentNode.get();
                currentNode.set(currentNode.get().goBack());
                engine.load(currentNode.get().location);
            }
        }

        public void goForward() {
            if (canGoForward()) {
                lastNode = currentNode.get();
                currentNode.set(currentNode.get().goForward());
                engine.load(currentNode.get().location);
            }
        }

        public void add(String s, String t) {
            if (!wentBack() && !wentForward()) {
                if (!currentNode.get().location.equals(s)) {
                    lastNode = null;
                    currentNode.set(currentNode.get().add(s, t));
                    entries.add(currentNode.get());
                }
            } else {
                lastNode = null;
                currentNode.get().updateTime();
            }
        }

        public boolean canGoForward() {
            return currentNode.get().canGoForward();
        }

        public boolean canGoBack() {
            return currentNode.get().canGoBack();
        }

        public boolean wentBack() {
            if (lastNode == null) {
                return false;
            }
            return lastNode.wentBack;
        }

        public boolean wentForward() {
            if (lastNode == null) {
                return false;
            }
            return lastNode.wentForward;
        }

        public void destroy() {
            root.destroy();
        }

    }

    public class Entry {

        private boolean wentBack = false, wentForward = false;
        private Entry parent;
        private ArrayList<Entry> children;
        private final int row;
        private Entry nextNode;
        private final String title;
        private final String location;
        private final ReadOnlyObjectWrapper<LocalDate> lastVisitedDate;
        private final ReadOnlyObjectWrapper<LocalTime> lastVisitedTime;

        Entry(Entry par, String loc, String title) {
            parent = par;
            children = new ArrayList<>();
            location = loc;
            this.title = title;
            if (par == null) {
                row = 0;
            } else {
                row = par.row + 1;
            }
            lastVisitedDate = new ReadOnlyObjectWrapper<>(LocalDate.now());
            lastVisitedTime = new ReadOnlyObjectWrapper<>(LocalTime.now());
        }

        public LocalDate getLastVisitedDate() {
            return lastVisitedDate.get();
        }

        public ReadOnlyObjectProperty<LocalDate> lastVisitedDateProperty() {
            return lastVisitedDate.getReadOnlyProperty();
        }

        public LocalTime getLastVisitedTime() {
            return lastVisitedTime.get();
        }

        public String getLocation() {
            return location;
        }

        public String getTitle() {
            return title;
        }

        public ReadOnlyObjectProperty<LocalTime> lastVisitedTimeProperty() {
            return lastVisitedTime.getReadOnlyProperty();
        }

        public void updateTime() {
            lastVisitedDate.set(LocalDate.now());
            lastVisitedTime.set(LocalTime.now());
        }

        public Entry getNextNode() {
            return nextNode;
        }

        public Entry goBack() {
            wentBack = true;
            return parent;
        }

        public Entry goForward() {
            wentForward = true;
            return nextNode;
        }

        public boolean canGoBack() {
            return parent != null;
        }

        public boolean canGoForward() {
            return !children.isEmpty();
        }

        public Entry add(String s, String t) {
            Entry n;
            children.add(n = new Entry(this, s, t));
            nextNode = n;
            return n;
        }

        public void toString(ArrayList<String> al) {
            al.add(location);
            for (Entry n : children) {
                n.toString(al);
            }
        }

        public void destroy() {
            if (!children.isEmpty()) {
                for (Entry n : children) {
                    n.destroy();
                }
                parent = null;
            }
            children = null;
        }
    }
}
