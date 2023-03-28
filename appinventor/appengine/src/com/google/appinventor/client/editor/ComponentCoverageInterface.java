package com.google.appinventor.client.editor;

import java.util.List;
import java.util.Map;

public interface ComponentCoverageInterface {

    public static class CoverageDefinition {

        public String getName() {
            return name;
        }

        public final String name;

        public final Map<String, Integer> android;

        public final List<String> events;

        public final Map<String, Integer> ios;

        public final List<String> methods;

        public final List<String> propertyGetters;

        public final List<String> propertySetters;

        public final String covergeJson;


        public CoverageDefinition(String name, Map<String, Integer> android, List<String> events, Map<String, Integer> ios, List<String> methods, List<String> propertyGetters, List<String> propertySetters, String covergeJson) {
            this.name = name;
            this.android = android;
            this.events = events;
            this.ios = ios;
            this.methods = methods;
            this.propertyGetters = propertyGetters;
            this.propertySetters = propertySetters;
            this.covergeJson = covergeJson;
        }

        public Map<String, Integer> getAndroid() {
            return android;
        }

        public List<String> getEvents() {
            return events;
        }

        public Map<String, Integer> getIos() {
            return ios;
        }

        public List<String> getMethods() {
            return methods;
        }

        public List<String> getPropertyGetters() {
            return propertyGetters;
        }

        public List<String> getPropertySetters() {
            return propertySetters;
        }

        public String getCovergeJson() {
            return covergeJson;
        }
    }

    /**
     * Returns the name of the component
     * @param componentType type of component to query
     * @return name of the component
     */
    String getComponentName(String componentType);

    /**
     * Returns the count of properties, methods, and events of a component in Android
     * @param componentName
     * @return
     */
    Map<String, Integer> getAndroidCount(String componentName);

    /**
     * Return the count of properties, methods, and events of a component in iOS
     * @param componentName
     * @return
     */
    Map<String, Integer> getIosCount(String componentName);
}
