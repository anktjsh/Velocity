/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import velocity.util.FileUtils;

/**
 *
 * @author Aniket
 */
public class SettingsManager {

    private static final TreeMap<String, StringProperty> properties = new TreeMap<>();
    private static final ArrayList<SettingsListener> listener = new ArrayList<>();

    public static void initProperty(String s, String defaultValue) {
        properties.put(s, new SimpleStringProperty(""));
        properties.get(s).addListener((ob, older, newer) -> {
            if (newer != null) {
                for (SettingsListener sl : listener) {
                    sl.settingsChanged(s, older, newer);
                }
            }
        });
    }

    public static void load() {
        ArrayList<String> al = new ArrayList<>();
        al.addAll(FileUtils.readAllLines(new File("properties.txt")));
        for (int x = 0; x < al.size(); x += 2) {
            if (properties.keySet().contains(al.get(x))) {
                properties.get(al.get(x)).set(al.get(x + 1));
            }
        }
    }

    public static String getProperty(String key) {
        return properties.get(key).get();
    }

    public static void addSettingsListener(SettingsListener sl) {
        listener.add(sl);
    }

    public static void removeSettingsListener(SettingsListener sl) {
        listener.remove(sl);
    }

    public static void setProperty(String a, String b) {
        if (properties.containsKey(a)) {
            properties.get(a).set(b);
        } else {
            throw new RuntimeException("Property not initialized");
        }
    }

    public static void save() {
        ArrayList<String> al = new ArrayList<>();
        for (String s : properties.keySet()) {
            al.add(s);
            al.add(properties.get(s).get());
        }
        FileUtils.write(new File("properties.txt"), al);
    }

    public static interface SettingsListener {

        public void settingsChanged(String settingsKey, String oldValue, String newValue);
    }
}
