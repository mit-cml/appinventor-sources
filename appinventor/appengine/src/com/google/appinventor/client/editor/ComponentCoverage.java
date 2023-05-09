package com.google.appinventor.client.editor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentCoverage implements ComponentCoverageInterface {

    private final HashMap<String, CoverageDefinition> components;

    private static final Map<Long, ComponentCoverage> instances = new HashMap<Long, ComponentCoverage>();

    private String coverageJsonString;

    public static ComponentCoverage getInstance(long projectId) {
        if (instances.containsKey(projectId)) {
            return instances.get(projectId);
        }
        ComponentCoverage newCoverage = new ComponentCoverage();
        instances.put(projectId, newCoverage);
        return newCoverage;
    }

    public static ComponentCoverage getInstance(){
        return getInstance(Ode.getInstance().getCurrentYoungAndroidProjectId());
    }

    public interface ComponentResource extends ClientBundle {
        @Source("com/google/appinventor/coverage.json")
        TextResource getComponentCoverage();
    }

    private static final ComponentResource componentResources = GWT.create(ComponentResource.class);

    private ComponentCoverage() {
        Map<String, JSONValue> coverage = new ClientJsonParser().parse(componentResources.getComponentCoverage().getText()).asObject().get("components").asObject().getProperties();
        components = new HashMap<String, CoverageDefinition>();
        for(Map.Entry<String, JSONValue> entry: coverage.entrySet()){
            String name = entry.getKey();
            JSONValue jsonValue = entry.getValue();
            Map<String, Integer> android = getCount(jsonValue.asObject().get("android").asObject());
            Map<String, Integer> ios = getCount(jsonValue.asObject().get("ios").asObject());
            List<String> events = getArray(jsonValue.asObject().get("events").asArray());
            List<String> methods = getArray(jsonValue.asObject().get("methods").asArray());
            List<String> propertyGetters = getArray(jsonValue.asObject().get("propertyGetters").asArray());
            List<String> propertySetters = getArray(jsonValue.asObject().get("propertySetters").asArray());
            CoverageDefinition coverageDefinition = new CoverageDefinition(name, 
                    android,events,ios, methods, propertyGetters, propertySetters, jsonValue.toJson());
            components.put(name, coverageDefinition);
        }
        coverageJsonString = generateComponentsJSON();
    }

    private Map<String, Integer> getCount(JSONObject android){
        Map<String, Integer> count = new HashMap<>();
        count.put("propertyCount", android.get("propertyCount").asNumber().getInt());
        count.put("methodCount", android.get("methodCount").asNumber().getInt());
        count.put("eventCount", android.get("eventCount").asNumber().getInt());
        return count;
    }
    
    private List<String> getArray(JSONArray jsonArray){
        List<String> array = new ArrayList<>();
        for(JSONValue jsonValue : jsonArray.getElements()){
            array.add(jsonValue.asString().getString());
        }
        return array;
    }

    private String generateComponentsJSON(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        String separator = "";
        for(Map.Entry<String, CoverageDefinition> comp : components.entrySet()){
            sb.append(separator).append("\""+comp.getValue().getName()+"\"");
            sb.append(":");
            sb.append(comp.getValue().getCovergeJson());
            separator=",";
        }
        sb.append("}");
        return sb.toString();
    }

    public String getCoverageJsonString() {
        return coverageJsonString;
    }


    @Override
    public String getComponentName(String componentName) {
        CoverageDefinition component = components.get(componentName);
        return component.getName();
    }

    @Override
    public Map<String, Integer> getAndroidCount(String componentName) {
        CoverageDefinition component = components.get(componentName);
        return component.getAndroid();
    }

    @Override
    public Map<String, Integer> getIosCount(String componentName) {
        CoverageDefinition component = components.get(componentName);
        return component.getIos();
    }

    public boolean isAndroidCompatible(String componentName){
        Map<String, Integer> androidCount = getAndroidCount(componentName);
        boolean android = false;
        for(Map.Entry<String,Integer> entry: androidCount.entrySet()){
            if(entry.getValue()!=0){
                android|= true;
            }
        }
        return android;
    }

    public boolean isIosCompatible(String componentName){
        Map<String, Integer> iosCount = getIosCount(componentName);
        boolean ios = false;
        for(Map.Entry<String, Integer> entry: iosCount.entrySet()){
            if(entry.getValue()!=0){
                ios|=true;
            }
        }
        return ios;
    }

}
