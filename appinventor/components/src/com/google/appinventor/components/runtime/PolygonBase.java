// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.runtime.util.MapFactory;
import com.google.appinventor.components.runtime.util.MapFactory.MapFeatureVisitor;

/**
 * An abstract class to override properties relevant to any polygonal map features. Note that Circle
 * is included in this hierarchy because OSMDroid draws circles as 60-sided polygons.
 */
@SimpleObject
public abstract class PolygonBase extends MapFeatureBaseWithFill {
  public PolygonBase(MapFactory.MapFeatureContainer container, MapFeatureVisitor<Double> distanceComputation) {
    super(container, distanceComputation);
  }
}
