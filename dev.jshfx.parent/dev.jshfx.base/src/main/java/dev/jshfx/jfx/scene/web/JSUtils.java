package dev.jshfx.jfx.scene.web;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public final class JSUtils {

    private JSUtils() {
    }

    public static Map<String, String> getElementDataAttributes(WebEngine engine, String name) {
        Map<String, String> data = new HashMap<>();
        var doc = engine.getDocument();
        NodeList nodeList = doc.getElementsByTagName(name);

        if (nodeList.getLength() == 1) {
            Node node = nodeList.item(0);
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                Node attr = node.getAttributes().item(i);

                if (attr.getNodeName().startsWith("data-")) {
                    data.put(attr.getNodeName(), attr.getNodeValue());
                }
            }
        }

        return data;
    }

    public static String getLinkUrl(WebEngine engine, double x, double y) {
        JSObject jsobject = getElementFromPoint(engine, x, y);
        JSObject linkJsobject = JSUtils.getJSObject(jsobject, "a");

        String url = linkJsobject == null ? null : (String) linkJsobject.getMember("href");

        return url;
    }

    public static JSObject getElementFromPoint(WebEngine engine, double x, double y) {
        return (JSObject) engine.executeScript(String.format("document.elementFromPoint(%f , %f);", x, y));
    }

    public static String getSelection(WebEngine engine) {
        return engine.executeScript("window.getSelection().toString()").toString();
    }

    public static JSObject getJSObject(JSObject object, String tagName) {
        Node node = null;
        if (object instanceof Node) {
            node = (Node) object;
            while (node != null && !tagName.equalsIgnoreCase(node.getNodeName())) {
                node = node.getParentNode();
            }
        }

        return node instanceof JSObject && tagName.equalsIgnoreCase(node.getNodeName()) ? (JSObject) node : null;
    }

    public static int getInteger(JSObject obj, String attr, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt(obj.getMember(attr).toString());
        } catch (Exception e) {

        }

        return value;
    }
}
