package com.google.appinventor.components.runtime.ar;

import com.google.ar.core.Plane;

@FunctionalInterface
public interface NearestPlaneFinder {
    Plane find(float posX, float posZ);
}