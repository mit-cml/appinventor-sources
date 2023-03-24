//
//  Topology.swift
//
//  Created by Andrea Cremaschi on 22/05/15.
//  Copyright (c) 2015 andreacremaschi. All rights reserved.
//

import Foundation

/// Topological operations
public extension Geometry {

    /**
     - returns: A Polygon that represents all points whose distance from this geometry is less than or equal to the
                given width.
     */
    func buffer(width: Double) -> Geometry? {
        guard let bufferGEOM = GEOSBuffer_r(GEOS_HANDLE, storage.GEOSGeom, width, 0) else { return nil }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: bufferGEOM, parent: nil))
    }

    /// - returns: The smallest Polygon that contains all the points in the geometry.
    func convexHull() -> Polygon? {
        guard let convexHullGEOM = GEOSConvexHull_r(GEOS_HANDLE, storage.GEOSGeom) else { return nil }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: convexHullGEOM, parent: nil)) as? Polygon
    }

    /// - returns: a Geometry representing the points shared by this geometry and other.
    func intersection(_ geometry: Geometry) -> Geometry? {
        guard let intersectionGEOM = GEOSIntersection_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) else {
            return nil
        }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: intersectionGEOM, parent: nil))
    }

    /// - returns: A Geometry representing all the points in this geometry and the other.
    func union(_ geometry: Geometry) -> Geometry? {
        guard let unionGEOM = GEOSUnion_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) else { return nil }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: unionGEOM, parent: nil))
    }

    /// - returns: A Geometry representing all the points in this geometry and the other.
    func unaryUnion() -> Geometry? {
        guard let unionGEOM = GEOSUnaryUnion_r(GEOS_HANDLE, storage.GEOSGeom) else { return nil }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: unionGEOM, parent: nil))
    }

    /// - returns: A Geometry representing the points making up this geometry that do not make up other.
    func difference(_ geometry: Geometry) -> Geometry? {
        guard let differenceGEOM = GEOSDifference_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) else {
            return nil
        }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: differenceGEOM, parent: nil))
    }

    /// - returns: The boundary as a newly allocated Geometry object.
    func boundary() -> Geometry? {
        guard let boundaryGEOM = GEOSBoundary_r(GEOS_HANDLE, storage.GEOSGeom) else { return nil }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: boundaryGEOM, parent: nil))
    }

    /**
     - returns: A Waypoint representing the geometric center of the geometry. The point is not guaranteed to be on the
                interior of the geometry.
     */
    func centroid() -> Waypoint? {
        guard let centroidGEOM = GEOSGetCentroid_r(GEOS_HANDLE, storage.GEOSGeom) else { return nil }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: centroidGEOM, parent: nil)) as? Waypoint
    }

    /// - returns: A Polygon that represents the bounding envelope of this geometry,
    ///            or nil in the case of an empty geometry
    func envelope() -> Envelope? {
        guard let envelopeGEOM = GEOSEnvelope_r(GEOS_HANDLE, storage.GEOSGeom) else { return nil }
        let envStorage = GeometryStorage(GEOSGeom: envelopeGEOM, parent: nil)
        switch GEOSGeomTypeId_r(GEOS_HANDLE, envStorage.GEOSGeom) {
        case Polygon.geometryTypeId():
            return Envelope(storage: envStorage)
        case Waypoint.geometryTypeId():
            let wp = Waypoint(storage: envStorage)
            return Envelope(p1: wp.coordinate, p2: wp.coordinate)
        default:
            return nil
        }
    }

    /// - returns: A POINT guaranteed to lie on the surface.
    func pointOnSurface() -> Waypoint? {
        guard let pointOnSurfaceGEOM = GEOSPointOnSurface_r(GEOS_HANDLE, storage.GEOSGeom) else { return nil }
        return Geometry.create(storage: GeometryStorage(GEOSGeom: pointOnSurfaceGEOM, parent: nil)) as? Waypoint
    }

    /// - returns: The nearest point of this geometry with respect to `geometry`.
    func nearestPoint(_ geometry: Geometry) -> Coordinate {
        return nearestPoints(geometry)[0]
    }

    /// - returns: The nearest points in the geometries, ownership to caller.
    func nearestPoints(_ geometry: Geometry) -> [Coordinate] {
        let nearestPointsCoordinateList = GEOSNearestPoints_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom)
        var x0: Double = 0
        var y0: Double = 0
        GEOSCoordSeq_getX_r(GEOS_HANDLE, nearestPointsCoordinateList, 0, &x0)
        GEOSCoordSeq_getY_r(GEOS_HANDLE, nearestPointsCoordinateList, 0, &y0)

        var x1: Double = 0
        var y1: Double = 0
        GEOSCoordSeq_getX_r(GEOS_HANDLE, nearestPointsCoordinateList, 1, &x1)
        GEOSCoordSeq_getY_r(GEOS_HANDLE, nearestPointsCoordinateList, 1, &y1)

        return [Coordinate(x: x0, y: y0), Coordinate(x: x1, y: y1)]
    }

    /**
     - returns: The DE-9IM intersection matrix (a string) representing the topological relationship between this
                geometry and the other.
     */
    func relationship(_ geometry: Geometry) -> String {
        guard let CString = GEOSRelate_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom) else { return "" }
        return String(cString: CString)
    }

    /// - returns: The area of this geometry, or nil on error
    func area() -> Double? {
        var area: Double = 0
        guard GEOSArea_r(GEOS_HANDLE, storage.GEOSGeom, &area) == 1 else { return nil }
        return area
    }
}
