/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import velocity.util.FileUtils;

/**
 *
 * @author Aniket
 */
public class SettingsManager {

    private static final TreeMap<String, String> properties = new TreeMap<>();

    public static void initProperty(String s, String defaultValue) {
        properties.put(s, defaultValue);
    }

    public static void load() {
        ArrayList<String> al = new ArrayList<>();
        al.addAll(FileUtils.readAllLines(new File("properties.txt")));
        for (int x = 0; x < al.size(); x += 2) {
            if (properties.keySet().contains(al.get(x))) {
                properties.put(al.get(x), al.get(x + 1));
            }
        }
    }

    public static String getProperty(String key) {
        return properties.get(key);
    }

    public static void setProperty(String a, String b) {
        properties.put(a, b);
    }

    public static void save() {
        ArrayList<String> al = new ArrayList<>();
        for (String s : properties.keySet()) {
            al.add(s);
            al.add(properties.get(s));
        }
        FileUtils.write(new File("properties.txt"), al);
    }
}
