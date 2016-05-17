/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 *
 * @author Aniket
 */
public class FavoritesManager {

    private static final ObservableMap<String, String> favorites = FXCollections.observableMap(new TreeMap<>());

    static {
        load();
    }

    public FavoritesManager() {
        Runtime.getRuntime().addShutdownHook(new Thread((() -> {
            save();
        })));
    }

    public boolean add(String name, String url) {
        if (favorites.containsKey(name)) {
            return false;
        }
        favorites.put(name, url);
        return true;
    }

    public ObservableMap<String, String> getFavorites() {
        return favorites;
    }

    private static FavoritesManager manager;

    public static FavoritesManager getInstance() {
        if (manager == null) {
            manager = new FavoritesManager();
        }
        return manager;
    }

    public static void load() {
        ArrayList<String> al = new ArrayList<>();
        try {
            al.addAll(Files.readAllLines(Paths.get("favorites.txt")));
        } catch (IOException e) {
        }
        for (String s : al) {
            String spl[] = s.split(",");
            favorites.put(spl[0], spl[1]);
        }
    }

    public void save() {
        ArrayList<String> al = new ArrayList<>();
        for (String s : favorites.keySet()) {
            al.add(s + "," + favorites.get(s));
        }
        try {
            Files.write(Paths.get("favorites.txt"), al);
        } catch (IOException e) {
        }
    }
}
