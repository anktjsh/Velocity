/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.manager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.util.Pair;
import velocity.core.History.Entry;
import velocity.core.VelocityEngine;
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
    private static final HashMap<VelocityEngine, HistoryChangeListener> listen = new HashMap<>();

    private class HistoryChangeListener implements ListChangeListener<Entry> {

        @Override
        public void onChanged(Change<? extends Entry> c) {
            c.next();
            if (c.wasAdded()) {
                for (Entry we : c.getAddedSubList()) {
                    WebEntry w;
                    entries.add(w = new WebEntry(we));
                    Website site = contains(frequency, we.getLocation());
//                    Website site = contains(frequency, we.getUrl());
                    if (site != null) {
                    } else {
                        frequency.add(site = new Website(w));
                    }
                    site.increment();
                }
            }
        }

    }

    private HistoryManager() {
    }

    public void addEngine(VelocityEngine web) {
        HistoryChangeListener lis = new HistoryChangeListener();
        listen.put(web, lis);
        web.getHistory().getEntries().addListener(lis);
    }

    public void removeEngine(VelocityEngine web) {
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

    public ArrayList<WebEntry> getEntries() {
        return entries;
    }

    public static class WebEntry {

        private final ObjectProperty<String> locationProperty;
        private final ObjectProperty<String> titleProperty;
        private final ObjectProperty<LocalDate> lastVisitedDate;
        private final ObjectProperty<LocalTime> timeVisited;

        public WebEntry(Entry ent) {
            locationProperty = new SimpleObjectProperty<>(ent.getLocation());
            titleProperty = new SimpleObjectProperty<>(ent.getTitle() == null ? "" : ent.getTitle());
            lastVisitedDate = new SimpleObjectProperty<>((ent.getLastVisitedDate()));
            timeVisited = new SimpleObjectProperty<>(LocalTime.now());
            ent.lastVisitedDateProperty().addListener((ob, older, newer) -> {
                lastVisitedDate.set((newer));
            });
            ent.lastVisitedTimeProperty().addListener((ob, older, newer) -> {
                timeVisited.set(newer);
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
        FileUtils.write(FileUtils.newFile("history.txt"), al);
    }

    public void load() {
        ArrayList<String> al = new ArrayList<>();
        al.addAll(FileUtils.readAllLines(FileUtils.newFile("history.txt")));
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
        frequency.clear();
    }

    public static HistoryManager getInstance() {
        return HistoryManagerHolder.INSTANCE;
    }

    private static class HistoryManagerHolder {

        private static final HistoryManager INSTANCE = new HistoryManager();
    }
}
