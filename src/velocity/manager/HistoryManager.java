/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.manager;

import java.io.File;
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
import javafx.util.Pair;
import velocity.util.FileUtils;

/**
 *
 * @author Aniket
 */
public class HistoryManager {

    private static class Website {

        private final WebEntry page;
        private int frequency;

        public Website(WebEntry s) {
            page = s;
            frequency = 0;
        }

        public WebEntry getPage() {
            return page;
        }

        public void increment() {
            frequency++;
        }

        public int getFrequency() {
            return frequency;
        }
    }

    private static final ArrayList<WebEntry> entries = new ArrayList<>();
    private static final ArrayList<Website> frequency = new ArrayList<>();
    private static final HashMap<WebEngine, List<WebEntry>> map = new HashMap<>();
    private static final HashMap<WebEngine, HistoryChangeListener> listen = new HashMap<>();

    private class HistoryChangeListener implements ListChangeListener<Entry> {

        private final WebEngine web;

        public HistoryChangeListener(WebEngine eng) {
            web = eng;
        }

        @Override
        public void onChanged(Change<? extends Entry> c) {
            c.next();
            if (c.wasAdded()) {
                for (WebHistory.Entry we : c.getAddedSubList()) {
                    WebEntry w;
                    entries.add(w = new WebEntry(we));
                    Website site = contains(frequency, we.getUrl());
                    if (site != null) {
                    } else {
                        frequency.add(site = new Website(w));
                    }
                    site.increment();
                    if (map.containsKey(web)) {
                        map.get(web).add(w);
                    }
                }
            }
        }

    }

    private HistoryManager() {
    }

    public void addEngine(WebEngine web) {
        map.put(web, new ArrayList<>());
        addAllEntries(web.getHistory().getEntries(), map.get(web));
        HistoryChangeListener lis = new HistoryChangeListener(web);
        listen.put(web, lis);
        web.getHistory().getEntries().addListener(lis);
    }

    public void removeEngine(WebEngine web) {
        map.remove(web);
        web.getHistory().getEntries().removeListener(listen.remove(web));
    }

    public ArrayList<Pair<String, String>> getFrequency() {
        ArrayList<Pair<String, String>> tree = new ArrayList<>();
        ArrayList<Website> temp = new ArrayList<>();
        while (temp.size() != frequency.size()) {
            temp.add(getMax(frequency, temp));
        }
        for (Website s : temp) {
            tree.add(new Pair<>(s.getPage().getTitle(), s.getPage().getLocation()));
        }
        return tree;
    }

    private static Website getMax(List<Website> a, List<Website> b) {
        int max = 0;
        Website temp = null;
        for (Website s : a) {
            if (s.getFrequency() >= max) {
                if (!b.contains(s)) {
                    max = s.getFrequency();
                    temp = s;
                }
            }
        }
        return temp;
    }

    private static Website contains(List<Website> list, String s) {
        for (Website w : list) {
            if (w.getPage().getLocation().equals(s)) {
                return w;
            }
        }
        return null;
    }

    public List<WebEntry> getHistory(WebEngine web) {
        return map.get(web);
    }

    private void addAllEntries(List<WebHistory.Entry> ent, List<WebEntry> went) {
        for (WebHistory.Entry we : ent) {
            WebEntry w;
            entries.add(w = new WebEntry(we));
            Website site = contains(frequency, we.getUrl());
            if (site != null) {
            } else {
                frequency.add(site = new Website(w));
            }
            site.increment();
            went.add(w);
        }
    }

    public ArrayList<WebEntry> getEntries() {
        return entries;
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

    public void save() {
        ArrayList<String> al = new ArrayList<>();
        for (WebEntry we : entries) {
            al.add(we.toString());
        }
        FileUtils.write(new File("history.txt"), al);
    }

    public void load() {
        ArrayList<String> al = new ArrayList<>();
        al.addAll(FileUtils.readAllLines(new File("history.txt")));
        for (String s : al) {
            String[] spl = s.split(",");
            if (spl.length == 4) {
                WebEntry w;
                entries.add(w = new WebEntry(spl[0], spl[1], LocalDate.parse(spl[2]), LocalTime.parse(spl[3])));
                Website site = contains(frequency, spl[0]);
                if (site != null) {
                } else {
                    frequency.add(site = new Website(w));
                }
                site.increment();
            }
        }
    }

    public void clear() {
        entries.clear();
        map.clear();
        frequency.clear();
    }

    public static HistoryManager getInstance() {
        return HistoryManagerHolder.INSTANCE;
    }

    private static class HistoryManagerHolder {

        private static final HistoryManager INSTANCE = new HistoryManager();
    }
}
