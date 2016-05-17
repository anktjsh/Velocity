/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;

/**
 *
 * @author Aniket
 */
public class HistoryManager {

    private static final ArrayList<WebEntry> entries = new ArrayList<>();
    private static final HashMap<WebEngine, List<WebEntry>> map = new HashMap<>();

    static {
        (new Thread(() -> {
            load();
        })).start();
    }

    private HistoryManager() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            save();
        }));
    }

    public void addEngine(WebEngine web) {
        map.put(web, new ArrayList<>());
        addAllEntries(web.getHistory().getEntries(), map.get(web));
        web.getHistory().getEntries().addListener((ListChangeListener.Change<? extends Entry> c) -> {
            c.next();
            if (c.wasAdded()) {
                for (WebHistory.Entry we : c.getAddedSubList()) {
                    WebEntry w;
                    entries.add(w = new WebEntry(we));
                    map.get(web).add(w);
                }
            }
        });
    }

    public List<WebEntry> getHistory(WebEngine web) {
        return map.get(web);
    }

    private void addAllEntries(List<WebHistory.Entry> ent, List<WebEntry> went) {
        for (WebHistory.Entry we : ent) {
            WebEntry w;
            entries.add(w = new WebEntry(we));
            went.add(w);
        }
    }

    public ArrayList<WebEntry> getEntries() {
        return entries;
    }

    public void addEntry(String a, String b, LocalDate d, LocalTime t) {
        entries.add(new WebEntry(a, b, d, t));
    }

    public static class WebEntry {

        private final ObjectProperty<String> locationProperty;
        private final ObjectProperty<String> titleProperty;
        private final ObjectProperty<LocalDate> lastVisitedDate;
        private final ObjectProperty<LocalTime> timeVisited;

        public WebEntry(WebHistory.Entry ent) {
            locationProperty = new SimpleObjectProperty<>(ent.getUrl());
            titleProperty = new SimpleObjectProperty<>(ent.getTitle() == null ? "" : ent.getTitle());
            lastVisitedDate = new SimpleObjectProperty<>(getLocalDate(ent.getLastVisitedDate()));
            timeVisited = new SimpleObjectProperty<>(LocalTime.now());
            ent.titleProperty().addListener((ob, older, newer) -> {
                titleProperty.set(newer);
            });
            ent.lastVisitedDateProperty().addListener((ob, older, newer) -> {
                lastVisitedDate.set(getLocalDate(newer));
            });
        }

        public WebEntry(String a, String b, LocalDate d, LocalTime now) {
            locationProperty = new SimpleObjectProperty<>(a);
            titleProperty = new SimpleObjectProperty<>(b);
            lastVisitedDate = new SimpleObjectProperty<>(d);
            timeVisited = new SimpleObjectProperty<>(now);
        }

        public String getLocation() {
            return locationProperty.get();
        }

        public String getTitle() {
            return titleProperty.get();
        }

        public LocalDate getLastVisitedDate() {
            if (lastVisitedDate.get() == null) {
                return LocalDate.now();
            }
            return lastVisitedDate.get();
        }

        public LocalTime getTimeVisited() {
            return timeVisited.get();
        }

        private LocalDate getLocalDate(Date dt) {
            if (dt != null) {
                return Instant.ofEpochMilli(dt.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            }
            return null;
        }

        @Override
        public String toString() {
            return getLocation() + "," + getTitle() + "," + getLastVisitedDate().toString() + "," + getTimeVisited().toString();
        }
    }

    private void save() {
        System.out.println(entries.size());
        ArrayList<String> al = new ArrayList<>();
        for (WebEntry we : entries) {
            al.add(we.toString());
        }
        try {
            Files.write(Paths.get("history.txt"), al);
        } catch (IOException ex) {
        }
    }

    private static void load() {
        ArrayList<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(Paths.get("history.txt")));
        } catch (IOException ex) {
        }
        for (String s : al) {
            String[] spl = s.split(",");
            if (spl.length == 4) {
                entries.add(new WebEntry(spl[0], spl[1], LocalDate.parse(spl[2]), LocalTime.parse(spl[3])));
            }
        }
    }

    public void clear() {
        entries.clear();
    }

    public static HistoryManager getInstance() {
        return HistoryManagerHolder.INSTANCE;
    }

    private static class HistoryManagerHolder {

        private static final HistoryManager INSTANCE = new HistoryManager();
    }
}
