//
//  LinearRef.swift
//
//  Created by Andrea Cremaschi on 03/02/16.
//  Copyright (c) 2016 andreacremaschi. All rights reserved.
//

import Foundation

/**
 Linear referencing functions
 */
public extension LineString {

    /// - returns: The distance of a point projected on the calling line
    public func distanceFromOriginToProjectionOfPoint(point: Waypoint) -> Double {
        return GEOSProject_r(GEOS_HANDLE, storage.GEOSGeom, point.storage.GEOSGeom)
    }

    public func normalizedDistanceFromOriginToProjectionOfPoint(point: Waypoint) -> Double {
        return GEOSProjectNormalized_r(GEOS_HANDLE, storage.GEOSGeom, point.storage.GEOSGeom)
    }

    /// Return closest point to given distance within geometry
    public func interpolatePoint(distance: Double) -> Waypoint? {
        guard let interpolatedPoint = GEOSInterpolate_r(GEOS_HANDLE, storage.GEOSGeom, distance) else {
            return nil
        }
        return Waypoint(storage: GeometryStorage(GEOSGeom: interpolatedPoint, parent: nil))
    }

    public func interpolatePoint(fraction: Double) -> Waypoint? {
        guard let interpolatedPoint = GEOSInterpolateNormalized_r(GEOS_HANDLE, storage.GEOSGeom, fraction) else {
            return nil
        }
        return Waypoint(storage: GeometryStorage(GEOSGeom: interpolatedPoint, parent: nil))
    }

    public func middlePoint() -> Waypoint? {
        return self.interpolatePoint(fraction: 0.5)
    }
}
