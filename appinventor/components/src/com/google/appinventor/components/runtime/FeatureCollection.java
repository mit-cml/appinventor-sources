// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureCollection;
import com.google.appinventor.components.runtime.util.YailList;

import android.view.View;
import org.json.JSONException;

@DesignerComponent(version = YaVersion.FEATURE_COLLECTION_COMPONENT_VERSION,
    category = ComponentCategory.MAPS,
    description = "A FeatureColletion contains one or more map features as a group. Any events " +
        "fired on a feature in the collection will also trigger the corresponding event on the " +
        "collection object. FeatureCollections can be loaded from external resources as a means " +
        "of populating a Map with content.")
@SimpleObject
public class FeatureCollection extends MapFeatureContainerBase implements MapFeatureCollection {
  private String source = "";
  private Map map;

  public FeatureCollection(MapFactory.MapFeatureContainer container) {
    super(container);
    map = container.getMap();
  }

  @SuppressWarnings("squid:S00100")
  @DesignerProperty
  @SimpleProperty(description = "Loads a collection of features from the given string. If the " +
      "string is not valid GeoJSON, the ErrorLoadingFeatureCollection error will be run with " +
      "url = <string>.")
  public void FeaturesFromGeoJSON(String geojson) {
    try {
      processGeoJSON("<string>", geojson);
    } catch(JSONException e) {
      $form().dispatchErrorOccurredEvent(this, "FeaturesFromGeoJSON",
          ErrorMessages.ERROR_INVALID_GEOJSON, e.getMessage());
    }
  }

  @Override
  @SimpleEvent(description = "A GeoJSON document was successfully read from url. The features " +
      "specified in the document are provided as a list in features.")
  public void GotFeatures(String url, YailList features) {
    source = url;
    super.GotFeatures(url, features);
  }

  @Override
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_GEOJSON_TYPE)
  public void Source(String source) {
    // Only set from the designer. Blocks will call {@link #LoadFromURL} instead.
    this.source = source;
  }

  @Override
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets or sets the source URL used to populate the feature collection. If " +
          "the feature collection was not loaded from a URL, this will be the empty string.")
  public String Source() {
    return source;
  }

  @Override
  public View getView() {
    // Even though we are an AndroidViewComponent, we don't actually have a view because the view
    // hierarchy is handled by the map controller.
    return null;
  }

  @Override
  public Map getMap() {
    return map;
  }
}
