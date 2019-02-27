//
//  GeoJSON.swift
//
//  Created by Andrea Cremaschi on 27/05/15.
//  Copyright (c) 2015 andreacremaschi. All rights reserved.
//

import Foundation

public enum GEOJSONParseError: Error {
    case invalidJSON
    case invalidGEOJSON
}

public extension Geometry {

    @available(*, deprecated: 2.1.0, renamed: "Features.fromGeoJSON(_:)")
    public class func fromGeoJSON(_ URL: Foundation.URL) throws -> [Geometry]? {
        return try Features.fromGeoJSON(URL)?.first?.geometries
    }

    @available(*, deprecated: 2.1.0, renamed: "Features.fromGeoJSONDictionary(_:)")
    public class func fromGeoJSONDictionary(_ dictionary: [String: AnyObject]) -> [Geometry]? {
        return Features.fromGeoJSONDictionary(dictionary)?.first?.geometries
    }

}

public typealias Features = [Feature]

public extension Array where Element == Feature {
    /**
    Creates an `Array` of `Feature` instances from a GeoJSON file.
    
     - parameter URL: the URL pointing to the GeoJSON file.
    returns: An optional `Array` of `Feature` instances.
     */
    public static func fromGeoJSON(_ URL: Foundation.URL) throws -> Features? {
        guard let JSONData = try? Data(contentsOf: URL) else {
            return nil
        }
        return try fromGeoJSON(JSONData)
    }

    /**
     Creates an `Array` of `Geometry` instances from a GeoJSON string.
     
     - parameter string: the GeoJSON string.
     
     - returns: An optional `Array` of `Geometry` instances.
     */
    public static func fromGeoJSON(_ string: String) throws -> Features? {
        guard let data = string.data(using: .utf8) else {
            return nil
        }
        return try fromGeoJSON(data)
    }

    /**
     Creates an `Array` of `Geometry` instances from GeoJSON data.
     
     - parameter data: the GeoJSON data.
     
     - returns: An optional `Array` of `Geometry` instances.
     */
    public static func fromGeoJSON(_ data: Data) throws -> Features? {
        do {
            // read JSON file
            let parsedObject = try JSONSerialization.jsonObject(
                with: data,
                options: JSONSerialization.ReadingOptions.allowFragments) as? NSDictionary

            // is the root a Dictionary with a "type" key of value "FeatureCollection"?
            if let rootObject = parsedObject as? [String: AnyObject] {
                return fromGeoJSONDictionary(rootObject)
            } else {
                throw GEOJSONParseError.invalidGEOJSON
            }
        } catch _ {
            throw GEOJSONParseError.invalidJSON
        }
    }

    /**
    Creates an `Array` of `Feature` instances from a GeoJSON dictionary.
    
     - parameter dictionary: a dictionary following GeoJSON format specification.
    
    :returns: An optional `Array` of `Feature` instances.
    */
    public static func fromGeoJSONDictionary(_ dictionary: [String: AnyObject]) -> Features? {
        return ParseGEOJSONObject(dictionary)
    }
}

// MARK: - Private parsing functions

private func ParseGEOJSONObject(_ GEOJSONObject: [String: AnyObject]) -> Features? {

    if let type = GEOJSONObject["type"] as? String {

        // is there a "features" key of type NSArray?
        switch type {
        case "Feature":
            if let feature = ParseGEOJSONFeature(GEOJSONObject) {
                return [feature]
            }

        case "FeatureCollection":
            if let featureCollection = GEOJSONObject["features"] as? NSArray {
                return ParseGEOJSONFeatureCollection(featureCollection)
            }

        case "GeometryCollection":
            if let geometryCollection = GEOJSONObject["geometries"] as? NSArray {
                let feature = Feature()
                feature.geometries = ParseGEOJSONGeometryCollection(geometryCollection)
                return [feature]
            }

        default:
            if let coordinates = GEOJSONObject["coordinates"] as? NSArray,
                let geometry = ParseGEOJSONGeometry(type, coordinatesNSArray: coordinates) {
                let feature = Feature()
                feature.geometries?.append(geometry)
                return [feature]
            }
        }
    }
    return nil
}

private func ParseGEOJSONFeatureCollection(_ features: NSArray) -> [Feature]? {
    // map every feature representation to a Feature object
    var featuresArray = Features()
    for feature in features {
        if let feat1 = feature as? NSDictionary,
            let feat2 = feat1 as? [String: AnyObject],
            let featureObject = ParseGEOJSONFeature(feat2) {
                featuresArray.append(featureObject)
        } else {
            return nil
        }
    }
    return featuresArray
}

private func ParseGEOJSONFeature(_ GEOJSONFeature: [String: AnyObject]) -> Feature? {
    // map feature representaion to Feature Object with properties and GEOS geometry
    let id = GEOJSONFeature["id"]
    if let geometry = GEOJSONFeature["geometry"] as? [String: AnyObject],
        let properties = GEOJSONFeature["properties"] as? NSDictionary,
        let geometryType = geometry["type"] as? String,
        let geometryCoordinates = geometry["coordinates"] as? NSArray,
        let geometryObject = ParseGEOJSONGeometry(geometryType, coordinatesNSArray: geometryCoordinates) {
            let feature = Feature()
            feature.id = id
            feature.geometries?.append(geometryObject)
            feature.properties = properties
            return feature
    }
    return nil
}

private func ParseGEOJSONGeometryCollection(_ geometries: NSArray) -> [Geometry]? {
    // map every geometry representation to a GEOS geometry
    var GEOSGeometries = [Geometry]()
    for geometry in geometries {
        if let geom1 = geometry as? NSDictionary,
            let geom2 = geom1 as? [String: AnyObject],
            let geomType = geom2["type"] as? String,
            let geomCoordinates = geom2["coordinates"] as? NSArray,
            let geom = ParseGEOJSONGeometry(geomType, coordinatesNSArray: geomCoordinates) {
                GEOSGeometries.append(geom)
        } else {
            return nil
        }
    }
    return GEOSGeometries
}

// swiftlint:disable:next cyclomatic_complexity function_body_length
private func ParseGEOJSONGeometry(_ type: String, coordinatesNSArray: NSArray) -> Geometry? {
    switch type {
    case "Point":
        // For type "Point", the "coordinates" member must be a single position.
        guard let coordinates = coordinatesNSArray as? [Double],
            let sequence = GEOJSONSequenceFromArrayRepresentation([coordinates]),
            let GEOSGeom = GEOSGeom_createPoint_r(GEOS_HANDLE, sequence) else {
                return nil
        }
        return Waypoint(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))

    case "MultiPoint":
        // For type "MultiPoint", the "coordinates" member must be an array of positions.
        guard let coordinatesNSArray = coordinatesNSArray as? [[Double]] else {
            return nil
        }
        var pointArray = [Waypoint]()
        for singlePointCoordinates in coordinatesNSArray {
            guard let sequence = GEOJSONSequenceFromArrayRepresentation([singlePointCoordinates]),
                let GEOSGeom = GEOSGeom_createPoint_r(GEOS_HANDLE, sequence) else {
                    return nil
            }
            let point = Waypoint(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
            pointArray.append(point)
        }
        return MultiPoint(points: pointArray)

    case "LineString":
        // For type "LineString", the "coordinates" member must be an array of two or more positions.
        guard let coordinates = coordinatesNSArray as? [[Double]],
            let sequence = GEOJSONSequenceFromArrayRepresentation(coordinates),
            let GEOSGeom = GEOSGeom_createLineString_r(GEOS_HANDLE, sequence) else {
                return nil
        }
        return LineString(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))

    case "MultiLineString":
        // For type "MultiLineString", the "coordinates" member must be an array of LineString coordinate arrays.
        guard let linestringsRepresentation = coordinatesNSArray as? [[[Double]]] else {
            return nil
        }
        var linestrings: [LineString] = []
        for coordinates in linestringsRepresentation {
            guard let sequence = GEOJSONSequenceFromArrayRepresentation(coordinates),
                let GEOSGeom = GEOSGeom_createLineString_r(GEOS_HANDLE, sequence) else {
                    return nil
            }
            let linestring = LineString(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
            linestrings.append(linestring)
        }
        return MultiLineString(linestrings: linestrings)

    case "Polygon":
        return GEOJSONCreatePolygonFromRepresentation(coordinatesNSArray)

    case "MultiPolygon":
        // For type "MultiPolygon", the "coordinates" member must be an array of Polygon coordinate arrays.
        var polygons = [Polygon]()
        for representation in coordinatesNSArray {
            guard let polyRepresentation = representation as? NSArray,
                  let geom = GEOJSONCreatePolygonFromRepresentation(polyRepresentation) else {
                return nil
            }

            polygons.append(geom)
        }
        return MultiPolygon(polygons: polygons)

    default:
        return nil
    }
}

// MARK: 

private func GEOJSONGeometryFromDictionaryRepresentation(_ dictionary: [String: AnyObject]) -> Geometry? {

    if  let geometryDict = dictionary["geometry"] as? [String: AnyObject],
        let geometryType = geometryDict["type"] as? String {

        switch geometryType {
        case "GeometryCollection":
            // A geometry collection must have a "geometries" member, which is an array of GeoJSON geometry objects.
            if let geometriesNSArray = geometryDict["geometries"] as? NSArray,
                let geometriesArray = geometriesNSArray as? [NSDictionary] {
                var geometries = [Geometry]()
                for geometryNSDictionary in geometriesArray {
                    if let geometryType = geometryNSDictionary["type"] as? String,
                        let coordinatesNSArray = geometryNSDictionary["coordinates"] as? NSArray,
                        let geometry = ParseGEOJSONGeometry(geometryType, coordinatesNSArray: coordinatesNSArray) {
                        geometries.append(geometry)
                    } else {
                        return nil
                    }
                }
                return GeometryCollection(geometries: geometries)
            }
        default:
            if let coordinatesNSArray = geometryDict["coordinates"] as? NSArray {
                return ParseGEOJSONGeometry(geometryType, coordinatesNSArray: coordinatesNSArray)
            }
        }

    }
    return nil
}

private func GEOJSONCoordinatesFromArrayRepresentation(_ array: [[Double]]) -> [Coordinate]? {
    return array.map { coordinatePair in
        Coordinate(x: coordinatePair[0], y: coordinatePair[1])
    }
}

private func GEOJSONSequenceFromArrayRepresentation(_ representation: [[Double]]) -> OpaquePointer? {
    if let coordinates = GEOJSONCoordinatesFromArrayRepresentation(representation) {
        let sequence = GEOSCoordSeq_create_r(GEOS_HANDLE, UInt32(coordinates.count), 2)
        for (index, coord) in coordinates.enumerated() {
            if (GEOSCoordSeq_setX_r(GEOS_HANDLE, sequence, UInt32(index), coord.x) == 0) ||
                (GEOSCoordSeq_setY_r(GEOS_HANDLE, sequence, UInt32(index), coord.y) == 0) {
                    return nil
            }
        }
        return sequence
    }
    return nil
}

private func GEOJSONCreateLinearRingFromRepresentation(_ representation: [[Double]]) -> LinearRing? {
    guard let sequence = GEOJSONSequenceFromArrayRepresentation(representation),
        let GEOSGeom = GEOSGeom_createLinearRing_r(GEOS_HANDLE, sequence) else {
            return nil
    }
    return LinearRing(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
}

private func GEOJSONCreatePolygonFromRepresentation(_ representation: NSArray) -> Polygon? {

    // For type "Polygon", the "coordinates" member must be an array of LinearRing coordinate arrays.
    // For Polygons with multiple rings, the first must be the exterior ring and any others must be
    // interior rings or holes.

    if let coordinates = representation as? [[Double]] {
        // array of LinearRing coordinate arrays
        guard let shell = GEOJSONCreateLinearRingFromRepresentation(coordinates) else {
            return nil
        }
        let polygon = Polygon(shell: shell, holes: nil)
        return polygon
    }

    guard let ringsCoords = representation as? [[[Double]]], !ringsCoords.isEmpty else {
        return nil
    }

    // Polygons with multiple rings
    var rings = ringsCoords.compactMap { (ringCoords) -> LinearRing? in
        if let sequence = GEOJSONSequenceFromArrayRepresentation(ringCoords),
            let GEOSGeom = GEOSGeom_createLinearRing_r(GEOS_HANDLE, sequence) {
            return LinearRing(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
        } else if let GEOSGeom = GEOSGeom_createEmptyLineString_r(GEOS_HANDLE) {
            return LinearRing(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
        }
        return nil
    }
    let shell = rings.remove(at: 0)
    return Polygon(shell: shell, holes: rings)
}
