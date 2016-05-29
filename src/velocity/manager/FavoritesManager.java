/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.manager;

import java.util.ArrayList;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import velocity.util.FileUtils;

/**
 *
 * @author Aniket
 */
public class FavoritesManager {

    private static final ObservableMap<String, String> favorites = FXCollections.observableMap(new TreeMap<>());

    public FavoritesManager() {
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

    public void load() {
        ArrayList<String> al = new ArrayList<>();
        al.addAll(FileUtils.readAllLines(FileUtils.newFile("favorites.txt")));
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
        FileUtils.write(FileUtils.newFile("favorites.txt"), al);
    }
}
