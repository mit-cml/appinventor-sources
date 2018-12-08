// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

import java.util.ArrayList;
import java.util.List;

public class MockFeatureCollection extends MockContainer implements MockMapFeature {
  public static final String TYPE = "FeatureCollection";

  private static final String PROPERTY_NAME_SOURCE = "Source";
  private static final String PROPERTY_NAME_FEATURESFROMGEOJSON = "FeaturesFromGeoJSON";

  private MockMap map;
  private List<MockMapFeature> features = new ArrayList<MockMapFeature>();
  private JavaScriptObject collection;
  private boolean initialized = false;

  public MockFeatureCollection(SimpleEditor editor) {
    super(editor, TYPE, images.featurecollection(), new MockFeatureCollectionLayout());

    SimplePanel panel = new SimplePanel();
    panel.setWidth("16px");
    panel.setHeight("16px");
    panel.setStylePrimaryName("ode-SimpleMockComponent");
    Image icon = new Image(images.featurecollection());
    panel.add(icon);

    initComponent(panel);
    initCollection();
    initialized = true;
  }

  MockMap getMap() {
    return map;
  }

  @Override
  public void addToMap(MockMap map) {
    setVisible(false);  // MockFeatureCollection is managed by Leaflet
    layout.layoutWidth = 0;
    layout.layoutHeight = 0;
    this.map = map;
    setContainer(map);
    addToMap(map.getMapInstance());
    for (MockComponent component : getChildren()) {
      ((MockMapFeature) component).addToMap(map);
    }
  }

  @Override
  public boolean onDrop(MockMap map, int x, int y, int offsetX, int offsetY) {
    return true;
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyVisible(propertyName);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_SOURCE)) {
      setSourceProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_FEATURESFROMGEOJSON)) {
      setGeoJSONProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_VISIBLE)) {
      setVisibleProperty(newValue);
    }
  }

  @Override
  public void onRemoved() {
    for (MockMapFeature feature : features) {
      ((MockComponent) feature).onRemoved();
    }
    features.clear();
  }

  private void setSourceProperty(String text) {
    if (!initialized || !editor.isLoadComplete()) {
      return;
    }
    if (text == null || text.equals("")) {
      // Setting the property to null removes all children
      List<MockComponent> children = new ArrayList<MockComponent>(getChildren());
      for (MockComponent component : children) {
        removeComponent(component, true);
        component.onRemoved();
      }
      children.clear();
      features.clear();
      clearLayers();
      return;
    }
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    Ode.getInstance().getProjectService().load(projectId, "assets/" + text, new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        ErrorReporter.reportError(caught.getMessage());
      }

      @Override
      public void onSuccess(String result) {
        setGeoJSONProperty(result);
        MockFeatureCollection.this.onSelectedChange(true);  // otherwise the last imported component
                                                            // will be shown in the properties panel
      }
    });
  }

  private void setVisibleProperty(String text) {
    if (map == null) {
      // cannot change visibility if there is no map
      return;
    }
    boolean isVisible = Boolean.parseBoolean(text);
    setNativeVisible(map.getMapInstance(), isVisible);
  }

  private void setGeoJSONProperty(String text) {
    if (map == null) {
      // cannot change features if there is no map
      return;
    }
    setNativeGeoJSON(map.getMapInstance(), text);
  }

  @SuppressWarnings("unused")  // Called from JSNI
  private void onEachFeature(String type, JSONObject properties, JavaScriptObject layer) {
    if (type.equals("Point") || type.equals("MultiPoint")) {
      features.add(MockMarker.fromGeoJSON(this, properties, layer));
    } else if (type.equals("LineString") || type.equals("MultiLineString")) {
      features.add(MockLineString.fromGeoJSON(this, properties, layer));
    } else if (type.equals("Polygon") || type.equals("MultiPolygon")) {
      features.add(MockPolygon.fromGeoJSON(this, properties, layer));
    } else if (type.equals("Circle")) {
      features.add(MockCircle.fromGeoJSON(this, properties, layer));
    }
  }

  // JSNI Methods
  private native void initCollection()/*-{
    this.@com.google.appinventor.client.editor.simple.components.MockFeatureCollection::collection =
      top.L.featureGroup();
  }-*/;

  private native void addToMap(JavaScriptObject map)/*-{
    var collection = this.@com.google.appinventor.client.editor.simple.components.MockFeatureCollection::collection;
    map.addLayer(collection);
  }-*/;

  private native void clearLayers()/*-{
    var collection = this.@com.google.appinventor.client.editor.simple.components.MockFeatureCollection::collection;
    if (collection) {
      collection.clearLayers();
    }
  }-*/;

  private native void setNativeGeoJSON(JavaScriptObject map, String geojson)/*-{
    var collection = this.@com.google.appinventor.client.editor.simple.components.MockFeatureCollection::collection;
    if (collection) {
      var self = this;
      map.removeLayer(collection);
      if (geojson.charCodeAt(0) == 0xFEFF) geojson = geojson.substr(1);  // strip byte order marker, if present
      collection = top.L.geoJson(JSON.parse(geojson), {
        pointToLayer: function(feature, latlng) {
          for (var key in feature.properties) {
            var lowerKey = key.toLowerCase();
            if (lowerKey == 'r' || lowerKey == 'radius') {
              return top.L.circle(latlng, feature.properties[key]);
            }
          }
          return top.L.marker(latlng, {draggable: true, keyboard: false, alt: 'Marker'});
        },
        style: function(feature) {
          return {
            color: '#000',
            weight: 2,
            fillColor: '#480',
            fillOpacity: 1,
            pointerEvents: 'auto'
          };
        },
        onEachFeature: function(feature, layer) {
          var properties = @com.google.gwt.json.client.JSONObject::new(Lcom/google/gwt/core/client/JavaScriptObject;)(
            feature.properties);
          self.@com.google.appinventor.client.editor.simple.components.MockFeatureCollection::onEachFeature(*)(
            layer instanceof top.L.Circle ? 'Circle' : feature.geometry.type, properties, layer);
        }
      });
//      collection.addTo(map);
      this.@com.google.appinventor.client.editor.simple.components.MockFeatureCollection::collection = collection;
    }
  }-*/;

  private native void setNativeVisible(JavaScriptObject map, boolean visible)/*-{
  }-*/;
  /*
    var collection = this.@com.google.appinventor.client.editor.simple.components.MockFeatureCollection::collection;
    if (collection) {
      if (visible) {
        map.addLayer(collection);
      } else {
        map.removeLayer(collection);
      }
    }
  }-*/;

}
