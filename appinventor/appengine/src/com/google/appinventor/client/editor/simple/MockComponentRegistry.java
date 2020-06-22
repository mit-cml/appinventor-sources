package com.google.appinventor.client.editor.simple;

import jsinterop.annotations.JsType;

import java.util.HashMap;
import java.util.Map;

@JsType
public class MockComponentRegistry {

    private static final Map<String, MockComponentFactory> registry = new HashMap<>();

    public static void register(String type, MockComponentFactory mockComponentFactory) {
        registry.put(type, mockComponentFactory);
    }

    public static boolean isPresent(String type) {
        return registry.containsKey(type);
    }

    public static MockComponentFactory getMockComponentFactory(String type) {
        return registry.get(type);
    }
}
