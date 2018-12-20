// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.HashSet;
import java.util.Set;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;

public final class MockMap extends MockContainer {
  public static final String TYPE = "Map";
  protected static final String PROPERTY_NAME_LATITUDE = "Latitude";
  protected static final String PROPERTY_NAME_LONGITUDE = "Longitude";
  protected static final String PROPERTY_NAME_MAP_TYPE = "MapType";
  protected static final String PROPERTY_NAME_CENTER_FROM_STRING = "CenterFromString";
  protected static final String PROPERTY_NAME_ZOOM_LEVEL = "ZoomLevel";
  protected static final String PROPERTY_NAME_SHOW_COMPASS = "ShowCompass";
  protected static final String PROPERTY_NAME_SHOW_ZOOM = "ShowZoom";
  protected static final String PROPERTY_NAME_SHOW_USER = "ShowUser";
  protected static final String PROPERTY_NAME_ENABLE_ROTATION = "EnableRotation";
  protected static final String PROPERTY_NAME_SHOW_SCALE = "ShowScale";
  protected static final String PROPERTY_NAME_SCALE_UNITS = "ScaleUnits";

  /**
   * The Widget wrapping the element where the map tiles will be rendered.
   */
  protected final AbsolutePanel mapWidget;

  /**
   * The JavaScript object representing the non-GWT maps renderer.
   */
  private JavaScriptObject mapInstance;

  /**
   * A JavaScript array containing the (1-indexed) tile layers used for maps.
   */
  private JavaScriptObject tileLayers;

  /**
   * Active base tile layer.
   */
  private JavaScriptObject baseLayer;

  /**
   * Set of event listeners that will be triggered on native map events.
   */
  private final Set<MockMapEventListener> listeners = new HashSet<MockMapEventListener>();

  // Settings for the internal maps component
  private double latitude = 42.359144;
  private double longitude = -71.093612;
  private int zoomLevel = 13;
  private int selectedTileLayer = 1;
  private boolean zoomControl = false;
  private boolean compassEnabled = false;
  private boolean userLocationEnabled = false;
  private boolean showScale = false;
  private int scaleUnits = 1;

  public MockMap(SimpleEditor editor) {
    super(editor, TYPE, images.map(), new MockMapLayout());
    initToolbarItems();

    rootPanel.setHeight("100%");

    mapWidget = new AbsolutePanel();
    mapWidget.setStylePrimaryName("ode-SimpleMockContainer");
    mapWidget.add(rootPanel);

    initComponent(mapWidget);
    mapWidget.addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent arg0) {
        if (arg0.isAttached()) {
          initPanel();
          invalidateMap();
          for (MockComponent child : children) {
            ((MockMapFeature) child).addToMap(MockMap.this);
          }
        }
      }
    });
  }

  public void addEventListener(MockMapEventListener listener) {
    listeners.add(listener);
  }

  public void removeEventListener(MockMapEventListener listener) {
    listeners.remove(listener);
  }

  @Override
  public int getPreferredWidth() {
    return ComponentConstants.VIDEOPLAYER_PREFERRED_WIDTH;
  }

  @Override
  public int getPreferredHeight() {
    return ComponentConstants.VIDEOPLAYER_PREFERRED_HEIGHT;
  }

  @Override
  public void onBrowserEvent(Event event) {
    if (isUnlocked()) {
      setShouldCancel(event, false);
    } else {
      super.onBrowserEvent(event);
    }
  }

  @Override
  protected boolean acceptableSource(DragSource source) {
    MockComponent component = null;
    if (source instanceof MockComponent) {
      component = (MockComponent) source;
    } else if (source instanceof SimplePaletteItem) {
      component = (MockComponent) source.getDragWidget();
    }
    return component instanceof MockMapFeature;
  }

  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";
    }
    MockComponentsUtil.setWidgetBackgroundColor(mapWidget, text);
  }

  private void setEnabledProperty(String text) {
    MockComponentsUtil.setEnabled(this, text);
  }

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_ENABLED)) {
      setEnabledProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_LATITUDE)) {
      setLatitude(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_LONGITUDE)) {
      setLongitude(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_WIDTH)) {
      invalidateMap();
    } else if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      invalidateMap();
    } else if (propertyName.equals(PROPERTY_NAME_MAP_TYPE)) {
      setMapType(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_CENTER_FROM_STRING)) {
      setCenter(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ZOOM_LEVEL)) {
      setZoomLevel(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_COMPASS)) {
      setShowCompass(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_USER)) {
      setShowUser(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_ZOOM)) {
      setShowZoom(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_SCALE)) {
      setShowScale(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SCALE_UNITS)) {
      setScaleUnits(newValue);
    }
  }

  public final JavaScriptObject getMapInstance() {
    return mapInstance;
  }

  private void setLatitude(String text) {
    latitude = Double.parseDouble(text);
    updateMapLatitude(latitude);
  }

  private void setLongitude(String text) {
    longitude = Double.parseDouble(text);
    updateMapLongitude(longitude);
  }

  private void setMapType(String tileLayerId) {
    try {
      selectedTileLayer = Integer.parseInt(tileLayerId);
      updateMapType(selectedTileLayer);
    } catch(NumberFormatException e) {
      ErrorReporter.reportError(MESSAGES.unknownMapTypeException(tileLayerId));
      changeProperty(PROPERTY_NAME_MAP_TYPE, Integer.toString(selectedTileLayer));
    }
  }

  private void setCenter(String center) {
    String[] parts = center.split(",");
    if (parts.length != 2) {
      ErrorReporter.reportError(MESSAGES.mapCenterWrongNumberArgumentsException(parts.length));
      changeProperty(PROPERTY_NAME_CENTER_FROM_STRING, latitude + ", " + longitude);
    } else {
      latitude = Double.parseDouble(parts[0].trim());
      longitude = Double.parseDouble(parts[1].trim());
      updateMapCenter(latitude, longitude);
    }
  }

  private void setZoomLevel(String zoom) {
    int zoomLevel = Integer.parseInt(zoom);
    if (zoomLevel < 1 || zoomLevel > 18) {
      ErrorReporter.reportError(MESSAGES.mapZoomLevelOutOfBoundsException());
      changeProperty(PROPERTY_NAME_ZOOM_LEVEL, Integer.toString(this.zoomLevel));
    } else {
      this.zoomLevel = zoomLevel;
      updateMapZoomLevel(Integer.parseInt(zoom));
    }
  }

  private void setShowCompass(String state) {
    this.compassEnabled = Boolean.parseBoolean(state);
    updateMapCompassControl(this.compassEnabled);
  }

  private void setShowUser(String state) {
    this.userLocationEnabled = Boolean.parseBoolean(state);
    updateMapShowUser(this.userLocationEnabled);
  }

  private void setShowZoom(String state) {
    this.zoomControl = Boolean.parseBoolean(state);
    updateMapZoomControl(this.zoomControl);
  }

  private void setShowScale(String state) {
    this.showScale = Boolean.parseBoolean(state);
    updateMapShowScale(this.showScale);
  }

  private void setScaleUnits(String state) {
    if (state.equals("1")) {
      this.scaleUnits = 1;
    } else if (state.equals("2")) {
      this.scaleUnits = 2;
    } else {
      throw new IllegalArgumentException("Unexpected value for scale: " + state);
    }
    updateScaleUnits(this.scaleUnits);
  }

  // event handlers
  protected void onBoundsChanged() {
    // TODO(ewpatton): Send incremental update to companion
    for (MockMapEventListener listener : listeners) {
      listener.onBoundsChanged();
    }
  }

  protected void onResetButtonClicked() {
    try {
      updateMapZoomLevel(zoomLevel);
      updateMapCenter(latitude, longitude);
    } catch(NumberFormatException e) {
      // this shouldn't happen in the normal use of the component
    }
    for (MockMapEventListener listener : listeners) {
      listener.onResetButtonClicked();
    }
  }

  protected void onLockButtonClicked() {
    // we are moving to an unlocked state
    for (MockMapEventListener listener : listeners) {
      listener.onLockButtonClicked();
    }
  }

  protected void onUnlockButtonClicked() {
    // we are moving to a locked state
    for (MockMapEventListener listener : listeners) {
      listener.onUnlockButtonClicked();
    }
  }

  protected void onSetInitialBoundsClicked() {
    final LatLng centerPoint = getCenter();
    final int zoom = getZoom();

    this.latitude = centerPoint.latitude;
    this.longitude = centerPoint.longitude;
    this.zoomLevel = zoom;

    properties.changePropertyValue("CenterFromString", centerPoint.toString());
    properties.changePropertyValue("ZoomLevel", Integer.toString(zoom));

    for (MockMapEventListener listener : listeners) {
      listener.onSetInitialBoundsClicked();
    }
  }

  // Native Javascript Methods (JSNI)
  /**
   * Initialize the controls for the AppInventor map toolbar.
   * These controls allow the user to:
   * <ul>
   * <li>change the drag behavior from the default of component reordering to panning the map.
   * <li>update the starting center and zoom level from the map viewport.
   * <li>reset the map viewport to the center and zoom level specified in the properties.
   * </ul>
   * This method will be called with every MockMap created, but will only instantiate a singleton
   * set of items.
   */
  private static native void initToolbarItems()/*-{
    var MESSAGES = @com.google.appinventor.client.Ode::MESSAGES;
    var L = $wnd.top.L;
    if (L.AI2Lock === undefined) {
      L.AI2Lock = L.ToolbarAction.extend({
        options: {
          toolbarIcon: {
            tooltip: MESSAGES.@com.google.appinventor.client.OdeMessages::mapLockMovementTooltip()()
          }
        },
        _createIcon: function(toolbar, container, args) {
          L.ToolbarAction.prototype._createIcon.call(this, toolbar, container, args);
          var lockIcon = L.DomUtil.create('i'),
            unlockIcon = L.DomUtil.create('i');
          lockIcon.setAttribute('class', 'fa fa-lock');
          lockIcon.setAttribute('aria-hidden', 'true');
          unlockIcon.setAttribute('class', 'fa fa-unlock');
          unlockIcon.setAttribute('aria-hidden', 'true');
          this.locked = false;
          L.DomUtil.addClass(this._link, 'unlocked');
          this._link.appendChild(lockIcon);
          this._link.appendChild(unlockIcon);
          var self = this;
          L.DomEvent.on(this._link, 'mousedown', function(e) {
            e.stopPropagation();
          });
          L.DomEvent.on(this._link, 'click', function(e) {
            self.locked = !self.locked;
            var map = self.toolbar._control._map;
            map.unlocked = !self.locked;
            var interactions = [map.dragging, map.touchZoom, map.doubleClickZoom, map.scrollWheelZoom, map.boxZoom, map.keyboard, map.tap];
            if (self.locked) {
              for (var i in interactions) interactions[i] && interactions[i].disable();
              L.DomUtil.addClass(self._link, 'locked');
              L.DomUtil.removeClass(self._link, 'unlocked');
              self._link.setAttribute('title', MESSAGES.@com.google.appinventor.client.OdeMessages::mapUnlockMovementTooltip()());
              map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::onUnlockButtonClicked()();
            } else {
              for (var i in interactions) interactions[i] && interactions[i].enable();
              L.DomUtil.addClass(self._link, 'unlocked');
              L.DomUtil.removeClass(self._link, 'locked');
              self._link.setAttribute('title', MESSAGES.@com.google.appinventor.client.OdeMessages::mapLockMovementTooltip()());
              map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::onLockButtonClicked()();
            }
          });
        }
      });
      L.AI2Center = L.ToolbarAction.extend({
        options: {
          toolbarIcon: {
            tooltip: MESSAGES.@com.google.appinventor.client.OdeMessages::mapSetInitialMapTooltip()()
          }
        },
        _createIcon: function(toolbar, container, args) {
          var icon = L.DomUtil.create('i');
          L.ToolbarAction.prototype._createIcon.call(this, toolbar, container, args);
          icon.setAttribute('class', 'fa fa-crosshairs');
          this._link.appendChild(icon);
          var self = this;
          L.DomEvent.on(this._link, 'click', function() {
            var javaMockMap = self.toolbar._control._map.owner;
            javaMockMap.@com.google.appinventor.client.editor.simple.components.MockMap::onSetInitialBoundsClicked()();
          });
        }
      });
      L.AI2Reset = L.ToolbarAction.extend({
        options: {
          toolbarIcon: {
            tooltip: MESSAGES.@com.google.appinventor.client.OdeMessages::mapResetBoundingBoxTooltip()()
          }
        },
        _createIcon: function(toolbar, container, args) {
          var icon = L.DomUtil.create('i');
          L.ToolbarAction.prototype._createIcon.call(this, toolbar, container, args);
          icon.setAttribute('class', 'fa fa-history');
          this._link.appendChild(icon);
          var self = this;
          L.DomEvent.on(this._link, 'click', $entry(function() {
            var javaMockMap = self.toolbar._control._map.owner;
            javaMockMap.@com.google.appinventor.client.editor.simple.components.MockMap::onResetButtonClicked()();
          }));
        }
      });
      L.Control.Compass = L.Control.extend({
        options: { position: 'topright' },
        onAdd: function () {
          var container = L.DomUtil.create('div', 'compass-control'),
              img = L.DomUtil.create('img');
          img.setAttribute('src', '/leaflet/assets/compass.svg');
          container.appendChild(img);
          return container;
        }
      });
      L.control.compass = function(options) {
        return new L.Control.Compass(options);
      };
      L.UserOverlay = L.Layer.extend({
        onAdd: function(map) {
          this._map = map;
          this._el = L.DomUtil.create('div', 'ai2-user-mock-location leaflet-zoom-hide');
          var img = L.DomUtil.create('img');
          this._el.appendChild(img);
          img.setAttribute('src', '/leaflet/assets/location.png');
          map.getPanes()['overlayPane'].appendChild(this._el);
          map.on('viewreset', this._reposition, this);
          this._reposition();
          return this._el;
        },
        onRemove: function(map) {
          map.getPanes().overlayPane.removeChild(this._el);
          map.off('resize', this._reposition);
        },
        _reposition: function(e) {
          var pos = this._map.latLngToLayerPoint(this._map.getCenter());
          L.DomUtil.setPosition(this._el, pos);
        }
      });
    }
  }-*/;

  public native LatLng getCenter()/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      var center = map.getCenter();
      return @com.google.appinventor.client.editor.simple.components.MockMap.LatLng::new(DD)(center.lat, center.lng);
    }
    return null;
  }-*/;

  public native int getZoom()/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    return map ? map.getZoom() : 0;
  }-*/;

  private native void initPanel()/*-{
    var L = $wnd.top.L;
    var tileLayers = [
      null,  // because AppInventor is 1-indexed, we leave element 0 as null
      L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
                  {minZoom: 0, maxZoom: 18,
                   attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors'}),
      L.tileLayer('http://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer/tile/{z}/{y}/{x}',
                  {minZoom: 0, maxZoom: 15,
                   attribution: 'Satellite imagery &copy; <a href="http://mapquest.com">USGS</a>'}),
      L.tileLayer('http://basemap.nationalmap.gov/ArcGIS/rest/services/USGSTopo/MapServer/tile/{z}/{y}/{x}',
                  {minZoom: 0, maxZoom: 15,
                   attribution: 'Map data &copy; <a href="http://www.usgs.gov">USGS</a>'})
    ];
    this.@com.google.appinventor.client.editor.simple.components.MockMap::tileLayers = tileLayers;
    this.@com.google.appinventor.client.editor.simple.components.MockMap::baseLayer =
      tileLayers[this.@com.google.appinventor.client.editor.simple.components.MockMap::selectedTileLayer];

    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      // map exists but may be invalid due to change in the dom, so invalidate and redraw
      map.invalidateSize(false);
    } else {
      var panel = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapWidget;
      var elem = panel.@com.google.gwt.user.client.ui.UIObject::getElement()();
      if (elem.firstElementChild != null) elem = elem.firstElementChild;
      var latitude = this.@com.google.appinventor.client.editor.simple.components.MockMap::latitude,
          longitude = this.@com.google.appinventor.client.editor.simple.components.MockMap::longitude,
          zoomControl = this.@com.google.appinventor.client.editor.simple.components.MockMap::zoomControl,
          zoom = this.@com.google.appinventor.client.editor.simple.components.MockMap::zoomLevel,
          showScale = this.@com.google.appinventor.client.editor.simple.components.MockMap::showScale,
          scaleUnits = this.@com.google.appinventor.client.editor.simple.components.MockMap::scaleUnits;
      map = L.map(elem, {zoomControl: false, editable: true}).setView([latitude, longitude], zoom);
      var messages = @com.google.appinventor.client.Ode::getMessages()();
      map.zoomControl = L.control.zoom({
        position: 'topleft',
        zoomInTitle: messages.@com.google.appinventor.client.OdeMessages::mapZoomIn()(),
        zoomOutTitle: messages.@com.google.appinventor.client.OdeMessages::mapZoomOut()()
      });
      if (zoomControl) {
        map.zoomControl.addTo(map);
      }
      var scaleOptions = {metric: true, imperial: false, position: 'bottomright'};
      if (scaleUnits == 2) {
        scaleOptions.metric = false;
        scaleOptions.imperial = true;
      }
      map.scaleControl = L.control.scale(scaleOptions);
      if (showScale) {
        map.scaleControl.addTo(map);
      }
      map.owner = this;
      map.unlocked = true;
      map.aiControls = new L.Toolbar.Control({position: 'bottomleft',
                                             actions: [ L.AI2Lock, L.AI2Center, L.AI2Reset ]});
      map.aiControls.addTo(map);
      map.compassLayer = L.control.compass();
      map.userLayer = new L.UserOverlay();
      map.on('mouseup click', function(e) {
        e = e.originalEvent;
        if (e.eventPhase !== 3) return;
        var el = e.target,
            overlay = this.getPanes()['overlayPane'],
            markers = this.getPanes()['markerPane'],
            background = this.getPanes()['tilePane'],
            container = this.getContainer();
        while (el && el.parentNode !== container) {
          if (el === overlay || el === markers) {
            // Overlays handle their own click events, but sometimes it propagates to the map and eventually GWT.
            // This is not desirable because it causes issues with the selected component.
            return;
          } else if (el === background) {
            this.owner.@com.google.appinventor.client.editor.simple.components.MockComponent::select()();
            return;
          }
          el = el.parentNode;
        }
      });
      this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance = map;
      setTimeout(function() {
        map.addLayer(map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::baseLayer);
        map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::updateMapZoomControl(*)(
          map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::zoomControl);
        map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::updateMapCompassControl(*)(
          map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::compassEnabled);
        map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::updateMapShowUser(*)(
          map.owner.@com.google.appinventor.client.editor.simple.components.MockMap::userLocationEnabled);
      });
    }
  }-*/;

  native void invalidateMap()/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {  // Map may not be initialized yet, e.g., during project load.
      setTimeout(function() {
        map.invalidateSize(false);
      }, 0);
    }
  }-*/;

  private native void updateMapLatitude(double latitude)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    var longitude = this.@com.google.appinventor.client.editor.simple.components.MockMap::longitude;
    map.panTo($wnd.top.L.latLng(latitude, longitude));
  }-*/;

  private native void updateMapLongitude(double longitude)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    var latitude = this.@com.google.appinventor.client.editor.simple.components.MockMap::latitude;
    map.panTo($wnd.top.L.latLng(latitude, longitude));
  }-*/;

  private native void updateMapCenter(double latitude, double longitude)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {  // Map may not be initialized yet, e.g., during project load.
      map.panTo([latitude, longitude], {animate: true});
    }
  }-*/;

  private native void updateMapType(int type)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    var tileLayers = this.@com.google.appinventor.client.editor.simple.components.MockMap::tileLayers;
    var baseLayer = this.@com.google.appinventor.client.editor.simple.components.MockMap::baseLayer;
    if (map && baseLayer && tileLayers) {
      if (0 < type && type < tileLayers.length) {
        map.removeLayer(baseLayer);
        baseLayer = tileLayers[type];
        map.addLayer(baseLayer);
        baseLayer.bringToBack();
        this.@com.google.appinventor.client.editor.simple.components.MockMap::baseLayer = baseLayer;
      }
    }
  }-*/;

  native LatLng projectFromXY(int x, int y)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      var result = map.containerPointToLatLng([x, y]);
      return @com.google.appinventor.client.editor.simple.components.MockMap.LatLng::new(DD)(result.lat, result.lng);
    }
  }-*/;

  private native void updateMapZoomLevel(int zoomLevel)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      map.setZoom(zoomLevel);
    }
  }-*/;

  private native void updateMapCompassControl(boolean enable)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      if (enable) {
        map.addControl(map.compassLayer);
      } else {
        map.removeControl(map.compassLayer);
      }
    }
  }-*/;

  private native void updateMapShowUser(boolean enable)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      if (enable) {
        map.addLayer(map.userLayer);
      } else {
        map.removeLayer(map.userLayer);
      }
    }
  }-*/;

  private native void updateMapZoomControl(boolean enable)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      if (!map.zoomControl) {
        map.zoomControl = $wnd.top.L.control.zoom();
      }
      if (enable) {
        map.zoomControl.addTo(map);
      } else {
        map.removeControl(map.zoomControl);
      }
    }
  }-*/;

  private native void updateMapShowScale(boolean enable)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      if (!map.scaleControl) {
        map.scaleControl = $wnd.top.L.control.scale({position: 'topleft'});
      }
      if (enable) {
        map.scaleControl.addTo(map);
      } else {
        map.removeControl(map.scaleControl);
      }
    }
  }-*/;

  private native void updateScaleUnits(int units)/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance,
      scaleVisible = this.@com.google.appinventor.client.editor.simple.components.MockMap::showScale;
    if (map) {
      if (scaleVisible) {
        map.removeControl(map.scaleControl);
      }
      map.scaleControl = $wnd.top.L.control.scale({
        metric: units == 1,
        imperial: units == 2,
        position: 'bottomright'
      });
      if (scaleVisible) {
        map.scaleControl.addTo(map);
      }
    }
  }-*/;

  private native boolean isUnlocked()/*-{
    var map = this.@com.google.appinventor.client.editor.simple.components.MockMap::mapInstance;
    if (map) {
      return map.unlocked;
    } else {
      return false;
    }
  }-*/;

  public static class LatLng {
    public double latitude;
    public double longitude;

    public LatLng(double latitude, double longitude) {
      this.latitude = latitude;
      this.longitude = longitude;
    }

    @Override
    public String toString() {
      return Double.toString(latitude) + ", " + Double.toString(longitude);
    }

    public native NativeLatLng toNative()/*-{
      return {
        lat: this.@com.google.appinventor.client.editor.simple.components.MockMap.LatLng::latitude,
        lng: this.@com.google.appinventor.client.editor.simple.components.MockMap.LatLng::longitude
      };
    }-*/;
  }

  public static class NativeLatLng extends JavaScriptObject {
    protected NativeLatLng() {}

    public final native double getLatitude()/*-{
      return this.lat;
    }-*/;

    public final native double getLongitude()/*-{
      return this.lng;
    }-*/;
  }

  public interface MockMapEventListener {
    void onBoundsChanged();
    void onResetButtonClicked();
    void onLockButtonClicked();
    void onUnlockButtonClicked();
    void onSetInitialBoundsClicked();
  }
}
