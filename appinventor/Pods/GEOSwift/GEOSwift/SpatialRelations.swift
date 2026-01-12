//
//  Predicates.swift
//
//  Created by Andrea Cremaschi on 10/06/15.
//  Copyright (c) 2015 andreacremaschi. All rights reserved.
//

import Foundation

/** 
Spatial predicates methods

All of the following spatial predicate methods take another Geometry instance (other) as a parameter and return a bool.
*/
public extension Geometry {

    /// - returns: TRUE if the geometry is spatially equal to `geometry`
    @objc public func equals(_ geometry: Geometry) -> Bool {
        return GEOSEquals_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /// - returns: TRUE if the geometry is spatially disjoint to `geometry`
    @objc public func disjoint(_ geometry: Geometry) -> Bool {
        return GEOSDisjoint_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /// - returns: TRUE if the geometry spatially touches `geometry`
    @objc public func touches(_ geometry: Geometry) -> Bool {
        return GEOSTouches_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /// - returns: TRUE if the geometry spatially intersects `geometry`
    @objc public func intersects(_ geometry: Geometry) -> Bool {
        return GEOSIntersects_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /// - returns: TRUE if the geometry spatially crosses `geometry`
    @objc public func crosses(_ geometry: Geometry) -> Bool {
        return GEOSCrosses_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /// - returns: TRUE if the geometry is spatially within `geometry`
    @objc public func within(_ geometry: Geometry) -> Bool {
        return GEOSWithin_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /// - returns: TRUE if the geometry spatially contains `geometry`
    @objc public func contains(_ geometry: Geometry) -> Bool {
        return GEOSContains_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /// - returns: TRUE if the geometry spatially overlaps `geometry`
    @objc public func overlaps(_ geometry: Geometry) -> Bool {
        return GEOSOverlaps_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /// - returns: TRUE if the geometry spatially covers `geometry`
    @objc public func covers(_ geometry: Geometry) -> Bool {
        return GEOSCovers_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) > 0
    }

    /**
    - parameter pattern: A String following the Dimensionally Extended Nine-Intersection Model (DE-9IM).
    
    - returns: TRUE if the geometry spatially relates `geometry`, by testing for intersections between the
               Interior, Boundary and Exterior of the two geometries as specified by the values in the pattern.
    */
    @objc public func relate(_ geometry: Geometry, pattern: String) -> Bool {
        return GEOSRelatePattern_r(GEOS_HANDLE,
                                   storage.GEOSGeom,
                                   geometry.storage.GEOSGeom,
                                   (pattern as NSString).utf8String) > 0
    }

}
