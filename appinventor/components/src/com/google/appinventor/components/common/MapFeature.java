
package com.google.appinventor.components.common;

public enum MapFeature {
    Circle("Circle"),
    LineString("LineString"),
    Marker("Marker"),
    Polygon("Polygon"),
    Rectangle("Rectangle");

    private String value;

    MapFeature(String feat) {
      this.value = feat;
    }

  private static final Map<String, MapFeature> lookup = new HashMap<>();

  static {
    for(MapFeature feat : MapFeature.values()) {
      lookup.put(feat.getValue(), feat);
    }
  }

  public static MapFeature get(int feat) {
    return lookup.get(feat)
  }
}