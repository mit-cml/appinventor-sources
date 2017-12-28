// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import java.util.Collections;
import java.util.List;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.shared.rpc.project.FileDescriptor;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

public class MockMarker extends MockMapFeatureBaseWithFill {
  public static final String TYPE = "Marker";
  public static final String PROPERTY_NAME_IMAGE_ASSET = "ImageAsset";
  public static final String PROPERTY_NAME_ANCHORHORIZONTAL = "AnchorHorizontal";
  public static final String PROPERTY_NAME_ANCHORVERTICAL = "AnchorVertical";
  private String imageAsset = null;
  private String svgContent = null;
  private int width = LENGTH_PREFERRED;
  private int height = LENGTH_PREFERRED;
  private int srcWidth = ComponentConstants.MARKER_PREFERRED_WIDTH;
  private int srcHeight = ComponentConstants.MARKER_PREFERRED_HEIGHT;
  private double anchorU = 0.5;
  private double anchorV = 1.0;
  private JavaScriptObject nativeIcon;

  public MockMarker(SimpleEditor editor) {
    super(editor, TYPE, images.marker());
    Image image = new Image();
    image.setResource(Ode.getImageBundle().marker());
    image.setWidth("30");
    image.setHeight("50");
    panel.setWidget(image);
    this.unsinkEvents(Event.MOUSEEVENTS);
    initIcon();
    initMarker(0, 0);
  }

  static MockMarker fromGeoJSON(MockFeatureCollection parent, JSONObject properties, JavaScriptObject layer) {
    MockMarker marker = new MockMarker(parent.editor);
    marker.feature = layer;
    marker.setContainer(parent);
    String name = null;
    boolean hadImageAsset = false;
    for (String key : properties.keySet()) {
      String value;
      if (key.equalsIgnoreCase(PROPERTY_NAME_STROKEWIDTH) || key.equalsIgnoreCase(CSS_PROPERTY_STROKEWIDTH)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_STROKEWIDTH, value);
        marker.onPropertyChange(PROPERTY_NAME_STROKEWIDTH, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_STROKECOLOR) || key.equalsIgnoreCase(CSS_PROPERTY_STROKE)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_STROKECOLOR, value);
        marker.onPropertyChange(PROPERTY_NAME_STROKECOLOR, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_FILLCOLOR) || key.equalsIgnoreCase(CSS_PROPERTY_FILL)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_FILLCOLOR, value);
        marker.onPropertyChange(PROPERTY_NAME_FILLCOLOR, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_ANCHORHORIZONTAL)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_ANCHORHORIZONTAL, value);
        marker.onPropertyChange(PROPERTY_NAME_ANCHORHORIZONTAL, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_ANCHORVERTICAL)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_ANCHORVERTICAL, value);
        marker.onPropertyChange(PROPERTY_NAME_ANCHORVERTICAL, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_WIDTH)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_WIDTH, value);
        marker.onPropertyChange(PROPERTY_NAME_WIDTH, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_HEIGHT)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_HEIGHT, value);
        marker.onPropertyChange(PROPERTY_NAME_HEIGHT, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_NAME)) {
        name = properties.get(key).isString().stringValue();
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_IMAGE_ASSET)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_IMAGE_ASSET, value);
        marker.onPropertyChange(PROPERTY_NAME_IMAGE_ASSET, value);
        hadImageAsset = true;
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_VISIBLE)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_VISIBLE, value);
        marker.onPropertyChange(PROPERTY_NAME_VISIBLE, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_TITLE)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_TITLE, value);
        marker.onPropertyChange(PROPERTY_NAME_TITLE, value);
      } else if (key.equalsIgnoreCase(PROPERTY_NAME_DESCRIPTION)) {
        value = properties.get(key).isString().stringValue();
        marker.changeProperty(PROPERTY_NAME_DESCRIPTION, value);
        marker.onPropertyChange(PROPERTY_NAME_DESCRIPTION, value);
      }
    }
    if (name == null) {
      name = marker.getPropertyValue(PROPERTY_NAME_TITLE);
    }
    name = name.replaceAll("[ \t]+", "_");
    if (name.equalsIgnoreCase("")) {
      name = ComponentsTranslation.getComponentName("Marker") + "1";
    }
    name = ensureUniqueName(name, parent.editor.getComponentNames());
    marker.changeProperty(PROPERTY_NAME_NAME, name);
    marker.onPropertyChange(PROPERTY_NAME_NAME, name);
    marker.getForm().fireComponentRenamed(marker, ComponentsTranslation.getComponentName("Marker"));
    if (!hadImageAsset) {
      marker.setImageAsset(null);
    }
    marker.preserveLayerData();
    return marker;
  }

  @Override
  public void addToMap(MockMap map) {
    if (panel.getWidget() != null) {
      // remove the marker image for dragging
      panel.remove(panel.getWidget());
    }
    panel.setVisible(false);
    this.map = map;
    addToMap(map.getMapInstance());
  }

  @Override
  public boolean onDrop(MockMap map, int x, int y, int offsetX, int offsetY) {
    y += ComponentConstants.MARKER_PREFERRED_HEIGHT / 2 + 1;
    MockMap.LatLng point = map.projectFromXY(x, y);
    changeProperty(MockMapFeature.PROPERTY_NAME_LATITUDE, Double.toString(point.latitude));
    changeProperty(MockMapFeature.PROPERTY_NAME_LONGITUDE, Double.toString(point.longitude));
    return true;
  }

  @Override
  public void onRemoved() {
    setNativeVisible(map.getMapInstance(), false);
  }

  private void setLatitudeProperty(String newValue) {
    double latitude = Double.parseDouble(newValue);
    setLatLng(latitude, getLongitude());
  }

  private void setLongitudeProperty(String newValue) {
    double longitude = Double.parseDouble(newValue);
    setLatLng(getLatitude(), longitude);
  }

  private void setWidthProperty(String text) {
    try {
      int newWidth = Integer.parseInt(text);
      if (newWidth == LENGTH_FILL_PARENT) {
        width = map.getLayout().getLayoutWidth();
      } else if (newWidth <= LENGTH_PERCENT_TAG) {
        width = (int)((-newWidth - 1000) / 100.0 * map.getLayout().getLayoutWidth());
      } else if (newWidth == LENGTH_PREFERRED) {
        width = srcWidth;
      } else {
        width = newWidth;
      }
      setMarkerWidth(width);
    } catch(NumberFormatException e) {
      OdeLog.elog("Received bad value for MockMarker.setWidthProperty");
      OdeLog.xlog(e);
    }
  }

  private void setHeightProperty(String text) {
    try {
      int newHeight = Integer.parseInt(text);
      if (newHeight == LENGTH_FILL_PARENT) {
        height = map.getLayout().getLayoutHeight();
      } else if (newHeight == LENGTH_PERCENT_TAG) {
        height = (int) ((-newHeight - 1000) / 100.0 * map.getLayout().getLayoutHeight());
      } else if (newHeight == LENGTH_PREFERRED) {
        height = srcHeight;
      } else {
        height = newHeight;
      }
      setMarkerHeight(height);
    } catch(NumberFormatException e) {
      OdeLog.elog("Received bad value for MockMarker.setHeightProperty");
      OdeLog.xlog(e);
    }
  }

  private void setAnchorHorizontal(String text) {
    try {
      int newAlignment = Integer.parseInt(text);
      if (newAlignment < 1 || newAlignment > 3) {
        OdeLog.elog("Received invalid value of " + newAlignment + " in MockMarker.setAnchorHorizontal");
        return;
      }
      setMarkerAnchorHorizontal(newAlignment);
    } catch(NumberFormatException e) {
      OdeLog.elog("Received bad value for MockMarker.setAnchorHorizontal");
      OdeLog.xlog(e);
    }
  }

  private void setAnchorVertical(String text) {
    try {
      int newAlignment  = Integer.parseInt(text);
      if (newAlignment < 1 || newAlignment > 3) {
        OdeLog.elog("Received inalid value of " + newAlignment + " in MockMarker.setAnchorVertical");
        return;
      }
      setMarkerAnchorVertical(newAlignment);
    } catch(NumberFormatException e) {
      OdeLog.elog("Received bad value for MockMarker.setAnchorVertical");
      OdeLog.xlog(e);
    }
  }

  private void setImageAsset(String assetPath) {
    if (editor.getProjectId() == 0) {
      // not ready to set image assets
      return;
    }
    if (assetPath == null || assetPath.length() == 0) {
      svgContent = null;
      setMarkerAsset(null);
      return;
    }
    FileDescriptor descriptor = new FileDescriptor(editor.getProjectId(), "assets/" + assetPath);
    if (assetPath.endsWith(".svg")) {
      Ode.getInstance().getProjectService().load(Collections.singletonList(descriptor), new AsyncCallback<List<FileDescriptorWithContent>>() {
        @Override
        public void onFailure(Throwable arg0) {
          GWT.log("Error occurred loading image asset", arg0);
        }

        @Override
        public void onSuccess(List<FileDescriptorWithContent> arg0) {
          FileDescriptorWithContent fd = arg0.get(0);
          svgContent = fd.getContent();
          setMarkerAsset(svgContent);
        }
      });
    } else {
      svgContent = null;
      String url = convertImagePropertyValueToUrl(assetPath);
      if (url != null) {
        setMarkerAssetUrl(url);
      } else {
        setMarkerAsset(null);
      }
    }
  }

  private void setAnchor(double latitude, double longitude) {
    getProperties().changePropertyValue(PROPERTY_NAME_LATITUDE, Double.toString(latitude));
    getProperties().changePropertyValue(PROPERTY_NAME_LONGITUDE, Double.toString(longitude));
    super.onPropertyChange(PROPERTY_NAME_LATITUDE, Double.toString(latitude));
    super.onPropertyChange(PROPERTY_NAME_LONGITUDE, Double.toString(longitude));
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_LATITUDE)) {
      setLatitudeProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_LONGITUDE)) {
      setLongitudeProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_IMAGE_ASSET)) {
      setImageAsset(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_WIDTH)) {
      setWidthProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      setHeightProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ANCHORHORIZONTAL)) {
      setAnchorHorizontal(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ANCHORVERTICAL)) {
      setAnchorVertical(newValue);
    }
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.MARKER_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.MARKER_PREFERRED_HEIGHT;
  }

  // JSNI methods
  native void initIcon()/*-{
    var width = this.@com.google.appinventor.client.editor.simple.components.MockMarker::width,
      height = this.@com.google.appinventor.client.editor.simple.components.MockMarker::height,
      anchorU = this.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorU,
      anchorV = this.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorV,
      content = this.@com.google.appinventor.client.editor.simple.components.MockMarker::svgContent,
      opts = {
        icon: null,
        title: this.@com.google.appinventor.client.editor.simple.components.MockComponent::getName()(),
        className: "vector-marker ode-SimpleMockMapFeature leaflet-clickable",
        markerColor: this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBaseWithFill::fillColor,
        markerStroke: this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::strokeColor,
        iconSize: [width, height],
        iconAnchor: [width * anchorU, height * anchorV]
      };
    if (!content) {
      opts.map_pin = 'M25 0c-8.284 0-15 6.656-15 14.866 0 8.211 15 35.135 15 35.135s15-26.924 15-35.135c0-8.21-6.716-14.866-15-14.866zm-.049 19.312c-2.557 0-4.629-2.055-4.629-4.588 0-2.535 2.072-4.589 4.629-4.589 2.559 0 4.631 2.054 4.631 4.589 0 2.533-2.072 4.588-4.631 4.588z';
      opts.viewBox = [9, 0, 31, 50];
      opts.iconSize = [30, 50];
      opts.iconAnchor = [14, 49];
    } else {
      opts.svg = content;
    }
    this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon =
      $wnd.top.L.VectorMarkers.icon(opts);
  }-*/;

  native void initMarker(double latitude, double longitude)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
    this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature =
      $wnd.top.L.marker([latitude, longitude], {
        icon: icon,
        draggable: true,
        keyboard: false,
        alt: 'Marker'
      }).on('add', function() {
        if (this._icon) {
          var svg = this._icon.querySelector('svg');
          if (svg && !svg.getAttribute('preserveAspectRatio')) {
            svg.setAttribute('preserveAspectRatio', 'none');
          }
        }
      });
  }-*/;

  @Override
  protected native void setEditing(boolean editing)/*-{
    // Markers cannot be edited
  }-*/;

  protected native void setNativeTooltip(String tooltip)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
    if (icon) {
      icon.options.title = tooltip;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker && marker._icon) {
        marker._icon.style['title'] = tooltip;
      }
    }
  }-*/;

  protected native void setFillColor(String color)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
    if (icon) {
      icon.options.markerColor = color;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker && marker._icon) {
        marker._icon.style['fill'] = color;
      }
    }
  }-*/;

  protected native void setStrokeColor(String color)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
    if (icon) {
      icon.options.markerStroke = color;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker && marker._icon) {
        marker._icon.style['stroke'] = color;
      }
    }
  }-*/;

  protected native void setStrokeWidth(int weight)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (icon) {
      icon.options.markerWeight = weight;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker && marker._icon) {
        var svg = marker._icon.querySelector('svg');
        if (svg) {  // Attempt to scale the SVG stroke-width based on the Width/Height properties.
          try {
            var origWidth = this.@com.google.appinventor.client.editor.simple.components.MockMarker::srcWidth,
              origHeight = this.@com.google.appinventor.client.editor.simple.components.MockMarker::srcHeight,
              newWidth = this.@com.google.appinventor.client.editor.simple.components.MockMarker::width,
              newHeight = this.@com.google.appinventor.client.editor.simple.components.MockMarker::height,
              scale = 1;
            if (newWidth > 0) {
              scale = Math.min(scale, newWidth / origWidth);
            }
            if (newHeight > 0) {
              scale = Math.min(scale, newHeight / origHeight);
            }
            weight = weight / scale;
          } catch(e) {
            // Nothing to do here.
          }
        }
        marker._icon.style['stroke-width'] = weight + 'px';
      }
    }
  }-*/;

  native void setLatLng(double latitude, double longitude)/*-{
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (marker) {
      marker.setLatLng([latitude, longitude]);
    }
  }-*/;

  protected native void addToMap(JavaScriptObject map)/*-{
    var self = this;
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    map.addLayer(marker);
    if (!marker.clickHandler) {
      marker.clickHandler = function(e) {
        self.@com.google.appinventor.client.editor.simple.components.MockMarker::select()();
        if (e.originalEvent) e.originalEvent.stopPropagation();
      };
      marker.dragHandler = function(e) {
        var pt = marker.getLatLng();
        self.@com.google.appinventor.client.editor.simple.components.MockMarker::setAnchor(DD)(pt.lat, pt.lng);
      };
      marker.on('click dragstart', marker.clickHandler);
      marker.on('dragend', marker.dragHandler);
    }
  }-*/;

  native double getLatitude()/*-{
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (marker) {
      return marker.getLatLng().lat;
    } else {
      return undefined;
    }
  }-*/;

  native double getLongitude()/*-{
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (marker) {
      return marker.getLatLng().lng;
    } else {
      return undefined;
    }
  }-*/;

  protected native void setNativeVisible(JavaScriptObject map, boolean visible)/*-{
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (visible) {
      map.addLayer(marker);
    } else {
      map.removeLayer(marker);
    }
  }-*/;

  native void setMarkerAsset(String content)/*-{
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (!content) {
      this.@com.google.appinventor.client.editor.simple.components.MockMarker::initIcon()();
      var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
      if (marker && icon) {
        marker.setIcon(icon);
      }
    } else {
      var selected = this.@com.google.appinventor.client.editor.simple.components.MockComponent::isSelected()();
      var newW = this.@com.google.appinventor.client.editor.simple.components.MockMarker::width;
      if (newW === -1) {
        newW = this.@com.google.appinventor.client.editor.simple.components.MockMarker::srcWidth;
      }
      var newH = this.@com.google.appinventor.client.editor.simple.components.MockMarker::height;
      if (newH === -1) {
        newH = this.@com.google.appinventor.client.editor.simple.components.MockMarker::srcHeight;
      }
      var anchorU = this.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorU;
      var anchorV = this.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorV;
      var strokeWidth = selected ? 2 : this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::strokeWidth;
      var iconSize = [newW + 2 * strokeWidth, newH + 2 * strokeWidth];
      var newIcon = $wnd.top.L.VectorMarkers.icon({
        icon: null,
        svg: content,
        title: this.@com.google.appinventor.client.editor.simple.components.MockComponent::getName()(),
        className: selected ? "vector-marker ode-SimpleMockMapFeature-selected" : "vector-marker ode-SimpleMockMapFeature",
        markerColor: this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBaseWithFill::fillColor,
        markerStroke: this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::strokeColor,
        iconSize: iconSize,
        iconAnchor: [iconSize[0] * anchorU, iconSize[1] * anchorV],
        shadowSize: [iconSize[0] + 24, iconSize[1] + 1],
        shadowAnchor: [iconSize[0] * 0.5, iconSize[1]]
      });
      this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon = newIcon;
      if (marker) {
        marker.setIcon(newIcon);
        if (marker._icon) {
          var svg = marker._icon.querySelector('svg');
          var srcW = parseInt(svg.getAttribute('width'));
          var srcH = parseInt(svg.getAttribute('height'));
          // Makes the SVG icon scale like an img icon
          if (!svg.getAttribute('preserveAspectRatio')) {
            svg.setAttribute('preserveAspectRatio', 'none');
          }
          this.@com.google.appinventor.client.editor.simple.components.MockMarker::srcWidth = srcW;
          this.@com.google.appinventor.client.editor.simple.components.MockMarker::srcHeight = srcH;
          var resize = false;
          if (this.@com.google.appinventor.client.editor.simple.components.MockMarker::width === -1) {
            newW = srcW;
            resize = true;
          }
          if (this.@com.google.appinventor.client.editor.simple.components.MockMarker::height === -1) {
            newH = srcH;
            resize = true;
          }
          if (resize) {
            iconSize = [newW + 2 * strokeWidth, newH + 2 * strokeWidth];
            newIcon.options.iconSize = iconSize;
            newIcon.options.iconAnchor = [iconSize[0] * anchorU, iconSize[1] * anchorV];
            marker.setIcon(newIcon);
          }
          var scale = Math.min(newW / srcW, newH / srcH);
          marker._icon.style['stroke-width'] = (strokeWidth / scale) + 'px';
        }
      }
    }
  }-*/;

  native void setMarkerAssetUrl(String url)/*-{
    try {
      this.@com.google.appinventor.client.editor.simple.components.MockMarker::imageAsset = url;
      var L = $wnd.top.L;
      var self = this;
      var newIcon = new L.ImageIcon({
        iconUrl: url,
        className: 'ode-SimpleMockMapFeature',
        title: this.@com.google.appinventor.client.editor.simple.components.MockComponent::getName()(),
        onLoad: function(w, h) {
          // adjust w, h for selection border
          self.@com.google.appinventor.client.editor.simple.components.MockMarker::srcWidth = w;
          self.@com.google.appinventor.client.editor.simple.components.MockMarker::srcHeight = h;
          var newW = self.@com.google.appinventor.client.editor.simple.components.MockMarker::width;
          if (newW === -1) {
            newW = self.@com.google.appinventor.client.editor.simple.components.MockMarker::srcWidth;
          }
          var newH = self.@com.google.appinventor.client.editor.simple.components.MockMarker::height;
          if (newH === -1) {
            newH = self.@com.google.appinventor.client.editor.simple.components.MockMarker::srcHeight;
          }
          var anchorU = self.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorU;
          var anchorV = self.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorV;
          var strokeWidth = self.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::strokeWidth;
          newIcon.options.iconSize = [newW + 2 * strokeWidth, newH + 2 * strokeWidth];
          newIcon.options.iconAnchor = [newIcon.options.iconSize[0] * anchorU, newIcon.options.iconSize[1] * anchorV];
          newIcon.options.shadowAnchor = [0.5 * newIcon.options.iconSize[0], newIcon.options.iconSize[1]];
          var marker = self.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
          if (marker) {
            marker.setIcon(newIcon);
          }
        }
      });
      this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon = newIcon;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker) {
        marker.setIcon(newIcon);
      }
    } catch(e) {
      console.log(e);
    }
  }-*/;

  protected native void setSelected(boolean selected)/*-{
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    marker.options.icon.options.className = selected?"vector-marker ode-SimpleMockMapFeature-selected leaflet-clickable" : "vector-marker ode-SimpleMockMapFeature leaflet-clickable";
    marker.setIcon(marker.options.icon);
    if (marker._icon) {
      var svg = marker._icon.querySelector('svg');
      if (svg && !svg.getAttribute('preserveAspectRatio')) {
        svg.setAttribute('preserveAspectRatio', 'none');
      }
    }
  }-*/;

  native void setMarkerWidth(int width)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
    if (icon) {
      if (width === -1) {
        width = this.@com.google.appinventor.client.editor.simple.components.MockMarker::getIconPreferredSize()()[0];
      }
      icon.options.iconSize[0] = width;
      icon.options.iconAnchor[0] = this.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorU * width;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker) {
        marker.setIcon(icon);
      }
    }
  }-*/;

  native void setMarkerHeight(int height)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
    if (icon) {
      if (height === -1) {
        height = this.@com.google.appinventor.client.editor.simple.components.MockMarker::getIconPreferredSize()()[1];
      }
      icon.options.iconSize[1] = height;
      icon.options.iconAnchor[1] = this.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorV * height;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker) {
        marker.setIcon(icon);
      }
    }
  }-*/;

  native void setMarkerAnchorHorizontal(int align)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
    if (icon) {
      var adjust = 1.0;
      switch(align) {
      case 1:
        adjust = 0.0;
        break;
      case 2:
        adjust = 1.0;
        break;
      case 3:
        adjust = 0.5;
        break;
      }
      this.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorU = adjust;
      icon.options.iconAnchor[0] = adjust * icon.options.iconSize[0];
      icon.options.shadowAnchor[0] = (adjust * icon.options.iconSize[0]) + 18;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker) {
        marker.setIcon(icon);
      }
    }
  }-*/;

  native void setMarkerAnchorVertical(int align)/*-{
    var icon = this.@com.google.appinventor.client.editor.simple.components.MockMarker::nativeIcon;
    if (icon) {
      var adjust = 0.5;
      switch(align) {
      case 1:
        adjust = 0.0;
        break;
      case 2:
        adjust = 0.5;
        break;
      case 3:
        adjust = 1.0;
        break;
      }
      this.@com.google.appinventor.client.editor.simple.components.MockMarker::anchorV = adjust;
      icon.options.iconAnchor[1] = adjust * icon.options.iconSize[1];
      icon.options.shadowAnchor[1] = (adjust * icon.options.iconSize[1]) - 5;
      var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
      if (marker) {
        marker.setIcon(icon);
      }
    }
  }-*/;

  native JavaScriptObject getIconPreferredSize()/*-{
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (marker) {
      var img = marker._icon && marker._icon.querySelector('svg');
      if (img) {
        return [parseInt(img.getAttribute('width')), parseInt(img.getAttribute('height'))];
      }
      img = marker._icon && marker._icon.querySelector('img');
      if (img) {
        return [img.naturalWidth, img.naturalHeight];
      }
    }
    return [-1, -1];  // this is only called for when the width/height is -1, so this is idempotent.
  }-*/;

  private native void preserveLayerData()/*-{
    var marker = this.@com.google.appinventor.client.editor.simple.components.MockMapFeatureBase::feature;
    if (marker) {
      var pt = marker.getLatLng();
      this.@com.google.appinventor.client.editor.simple.components.MockMarker::setAnchor(DD)(pt.lat, pt.lng);
    }
  }-*/;
}
