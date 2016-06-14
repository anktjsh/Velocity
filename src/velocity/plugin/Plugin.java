/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velocity.plugin;

import javafx.scene.Node;
import javafx.util.Callback;
import javafx.util.Pair;
import velocity.core.VelocityEngine;

/**
 *
 * @author Aniket
 */
public class Plugin {

    public enum PluginFormat {
        URL, FILE_EXTENSION
    }

    private final PluginFormat format;
    private final String id;
    private final Callback<Pair<String, VelocityEngine>, Pair<Node, String>> node;

    public Plugin(PluginFormat format, String identifier, Callback<Pair<String, VelocityEngine>, Pair<Node, String>> cl) {
        this.format = format;
        id = identifier;
        node = cl;
    }

    public Pair<Node, String> getNodeAndTitle(VelocityEngine param, String url) {
        return node.call(new Pair<>(url, param));
    }

    public String getId() {
        return id;
    }

    public PluginFormat getFormat() {
        return format;
    }
}
