'use strict';

/* A Draggable that does not update the element position
and takes care of only bubbling to targetted path in Canvas mode. */
L.PathDraggable = L.Draggable.extend({

  initialize: function (path) {
    this._path = path;
    this._canvas = (path._map.getRenderer(path) instanceof L.Canvas);
    var element = this._canvas ? this._path._map.getRenderer(this._path)._container : this._path._path;
    L.Draggable.prototype.initialize.call(this, element, element, true);
  },

  _updatePosition: function () {
    var e = {originalEvent: this._lastEvent};
    this.fire('drag', e);
  },

  _onDown: function (e) {
    var first = e.touches ? e.touches[0] : e;
    this._startPoint = new L.Point(first.clientX, first.clientY);
    if (this._canvas && !this._path._containsPoint(this._path._map.mouseEventToLayerPoint(first))) { return; }
    L.Draggable.prototype._onDown.call(this, e);
  }

});


L.Handler.PathDrag = L.Handler.extend({

  initialize: function (path) {
    this._path = path;
  },

  getEvents: function () {
    return {
      dragstart: this._onDragStart,
      drag: this._onDrag,
      dragend: this._onDragEnd
    };
  },

  addHooks: function () {
    if (!this._draggable) { this._draggable = new L.PathDraggable(this._path); }
    this._draggable.on(this.getEvents(), this).enable();
    L.DomUtil.addClass(this._draggable._element, 'leaflet-path-draggable');
  },

  removeHooks: function () {
    this._draggable.off(this.getEvents(), this).disable();
    L.DomUtil.removeClass(this._draggable._element, 'leaflet-path-draggable');
  },

  moved: function () {
    return this._draggable && this._draggable._moved;
  },

  _onDragStart: function () {
    this._startPoint = this._draggable._startPoint;
    this._path
        .closePopup()
        .fire('movestart')
        .fire('dragstart');
  },

  _onDrag: function (e) {
    var path = this._path,
        event = (e.originalEvent.touches && e.originalEvent.touches.length === 1 ? e.originalEvent.touches[0] : e.originalEvent),
        newPoint = L.point(event.clientX, event.clientY),
        latlng = path._map.layerPointToLatLng(newPoint);

    this._offset = newPoint.subtract(this._startPoint);
    this._startPoint = newPoint;

    this._path.eachLatLng(this.updateLatLng, this);
    path.redraw();

    e.latlng = latlng;
    e.offset = this._offset;
    path.fire('move', e)
        .fire('drag', e);
  },

  _onDragEnd: function (e) {
    if (this._path._bounds) this.resetBounds();
    this._path.fire('moveend')
        .fire('dragend', e);
  },

  latLngToLayerPoint: function (latlng) {
    // Same as map.latLngToLayerPoint, but without the round().
    var projectedPoint = this._path._map.project(L.latLng(latlng));
    return projectedPoint._subtract(this._path._map.getPixelOrigin());
  },

  updateLatLng: function (latlng) {
    var oldPoint = this.latLngToLayerPoint(latlng);
    oldPoint._add(this._offset);
    var newLatLng = this._path._map.layerPointToLatLng(oldPoint);
    latlng.lat = newLatLng.lat;
    latlng.lng = newLatLng.lng;
  },

  resetBounds: function () {
    this._path._bounds = new L.LatLngBounds();
    this._path.eachLatLng(function (latlng) {
      this._bounds.extend(latlng);
    });
  }

});

L.Path.include({

  eachLatLng: function (callback, context) {
    context = context || this;
    var loop = function (latlngs) {
      for (var i = 0; i < latlngs.length; i++) {
        if (L.Util.isArray(latlngs[i])) loop(latlngs[i]);
        else callback.call(context, latlngs[i]);
      }
    };
    loop(this.getLatLngs ? this.getLatLngs() : [this.getLatLng()]);
  }

});

L.Path.addInitHook(function () {

  this.dragging = new L.Handler.PathDrag(this);
  if (this.options.draggable) {
    this.once('add', function () {
      this.dragging.enable();
    });
  }

});
