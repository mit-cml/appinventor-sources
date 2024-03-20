//
//  HumboldMapKit.swift
//
//  Created by Andrea Cremaschi on 26/05/15.
//  Copyright (c) 2015 andreacremaschi. All rights reserved.
//

import Foundation
import MapKit

extension Geometry {
    /// This method only works with GeometryCollection & its subclasses when the generic
    /// type is exatly one of Geometry, Polygon, Waypoint, LineString, LinearRing, or
    /// Envelope. This is because generics in Swift are invariant. See
    /// https://www.mikeash.com/pyblog/friday-qa-2015-11-20-covariance-and-contravariance.html
    public func mapShape() -> MKShape? {
        switch self {
        case is Waypoint:
            let pointAnno = MKPointAnnotation()
            pointAnno.coordinate = CLLocationCoordinate2D((self as! Waypoint).coordinate)
            return pointAnno
        case is LineString:
            var coordinates = (self as! LineString).points.map(CLLocationCoordinate2D.init)
            return MKPolyline(coordinates: &coordinates,
                              count: coordinates.count)
        case is Polygon:
            var exteriorRingCoordinates = (self as! Polygon).exteriorRing.points.map(CLLocationCoordinate2D.init)
            let interiorRings = (self as! Polygon).interiorRings.map {
                MKPolygonWithCoordinatesSequence($0.points)
            }
            return MKPolygon(coordinates: &exteriorRingCoordinates,
                             count: exteriorRingCoordinates.count,
                             interiorPolygons: interiorRings)
        case let gc as GeometryCollection<Geometry>:
            return MKShapesCollection(geometryCollection: gc)
        case let gc as GeometryCollection<Polygon>:
            return MKShapesCollection(geometryCollection: gc)
        case let gc as GeometryCollection<Waypoint>:
            return MKShapesCollection(geometryCollection: gc)
        case let gc as GeometryCollection<LineString>:
            return MKShapesCollection(geometryCollection: gc)
        case let gc as GeometryCollection<LinearRing>:
            return MKShapesCollection(geometryCollection: gc)
        case let gc as GeometryCollection<Envelope>:
            return MKShapesCollection(geometryCollection: gc)
        default:
            return nil
        }
    }
}

private func MKPolygonWithCoordinatesSequence(_ coordinates: CoordinatesCollection) -> MKPolygon {
    var coordinates = coordinates.map(CLLocationCoordinate2D.init)
    return MKPolygon(coordinates: &coordinates,
                     count: coordinates.count)

}

/** 
MKShape subclass for GeometryCollections.
The property `shapes` contains MKShape subclasses instances.
When drawing shapes on a map be careful to the fact that that these shapes could be overlays OR annotations.
*/
open class MKShapesCollection: MKShape, MKOverlay {
    public let shapes: [MKShape]
    public let centroid: CLLocationCoordinate2D
    public let boundingMapRect: MKMapRect

    required public init<T>(geometryCollection: GeometryCollection<T>) {
        let shapes = geometryCollection.geometries.compactMap { $0.mapShape() }

        if let coordinate = geometryCollection.centroid()?.coordinate {
            self.centroid = CLLocationCoordinate2D(coordinate)
        } else {
            self.centroid = CLLocationCoordinate2D(latitude: 0, longitude: 0)
        }

        self.shapes = shapes

        if let envelope = geometryCollection.envelope() {
            let topLeft = MKMapPoint(CLLocationCoordinate2D(envelope.topLeft))
            let bottomRight = MKMapPoint(CLLocationCoordinate2D(envelope.bottomRight))
            let mapRect = MKMapRect(origin: MKMapPoint(x: topLeft.x, y: topLeft.y),
                                    size: MKMapSize(width: bottomRight.x - topLeft.x,
                                                    height: bottomRight.y - topLeft.y))
            self.boundingMapRect = mapRect
        } else {
            self.boundingMapRect = .null
        }
    }
}

// MARK: - CLLocationCoordinate2D Conversions

extension CLLocationCoordinate2D {
    public init(_ coord: Coordinate) {
        self.init(latitude: coord.y, longitude: coord.x)
    }
}

extension Coordinate {
    public init(_ coord: CLLocationCoordinate2D) {
        self.init(x: coord.longitude, y: coord.latitude)
    }
}

// MARK: - Deprecated

@available(*, unavailable, renamed: "CLLocationCoordinate2D")
public func CLLocationCoordinate2DFromCoordinate(_ coord: Coordinate) -> CLLocationCoordinate2D {
    fatalError()
}

@available(*, unavailable, renamed: "Coordinate")
public func CoordinateFromCLLocationCoordinate2D(_ coord: CLLocationCoordinate2D) -> Coordinate {
    fatalError()
}
