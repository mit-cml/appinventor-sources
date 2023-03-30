//
//  GEOS.swift
//
//  Created by Andrea Cremaschi on 26/04/15.
//  Copyright (c) 2015 andreacremaschi. All rights reserved.
//

import Foundation

typealias GEOSCallbackFunction = @convention(c) (UnsafeMutableRawPointer) -> Void

let swiftCallback: GEOSCallbackFunction = {
    guard let string = String(validatingUTF8: $0.assumingMemoryBound(to: CChar.self)) else { return }
    print("GEOSwift # " + string + ".")
}

var GEOS_HANDLE: OpaquePointer = {
#if DEBUG
    guard let handle = GEOS_init_r() else { preconditionFailure() }
    GEOSContext_setNoticeMessageHandler_r(handle, unsafeBitCast(swiftCallback, to: GEOSMessageHandler_r.self), nil)
    GEOSContext_setErrorMessageHandler_r(handle, unsafeBitCast(swiftCallback, to: GEOSMessageHandler_r.self), nil)
    return handle
#else
    return GEOS_init_r()
#endif
}()

public typealias CoordinateDegrees = Double

final public class GeometryStorage {
    let GEOSGeom: OpaquePointer
    private let parent: GeometryStorage?

    init(GEOSGeom: OpaquePointer, parent: GeometryStorage?) {
        self.GEOSGeom = GEOSGeom
        self.parent = parent
    }

    deinit {
        if parent == nil {
            GEOSGeom_destroy_r(GEOS_HANDLE, GEOSGeom)
        }
    }
}

/// A base abstract geometry class
// Geometry is a model data type, so a struct would be a better fit, but it is actually a wrapper of GEOS native objects
// that are in fact C pointers, and structs in Swift don't have a dealloc where one can release allocated memory.
// Furthermore, being a class Geometry can inherit from NSObject so that debugQuickLookObject() can be implemented
open class Geometry: NSObject {

    let storage: GeometryStorage

    required public init(storage: GeometryStorage) {
        self.storage = storage
    }

    internal class func create(storage: GeometryStorage) -> Geometry? {
        return Geometry.classForGEOSGeom(storage.GEOSGeom)?.init(storage: storage)
    }

    open class func geometryTypeId() -> Int32 {
        return -1 // Abstract
    }

    public convenience init?(WKT: String) {
        let WKTReader = GEOSWKTReader_create_r(GEOS_HANDLE)
        defer { GEOSWKTReader_destroy_r(GEOS_HANDLE, WKTReader) }
        guard let GEOSGeom = GEOSWKTReader_read_r(GEOS_HANDLE, WKTReader, (WKT as NSString).utf8String),
            Geometry.classForGEOSGeom(GEOSGeom) === type(of: self) else {
                return nil
        }
        self.init(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
    }

    public convenience init?(WKB: [UInt8]) {
        var buffer = WKB
        guard let GEOSGeom = GEOSGeomFromWKB_buf_r(GEOS_HANDLE, &buffer, WKB.count),
            Geometry.classForGEOSGeom(GEOSGeom) === type(of: self) else {
                return nil
        }
        self.init(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
    }

    public convenience init?(data: Data) {
        guard let GEOSGeom = data.withUnsafeBytes({ GEOSGeomFromWKB_buf_r(GEOS_HANDLE, $0, data.count) }),
            Geometry.classForGEOSGeom(GEOSGeom) === type(of: self) else {
                return nil
        }
        self.init(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
    }

    private class func classForGEOSGeom(_ GEOSGeom: OpaquePointer) -> Geometry.Type? {
        let geometryTypeId = GEOSGeomTypeId_r(GEOS_HANDLE, GEOSGeom)
        var subclass: Geometry.Type

        switch UInt32(geometryTypeId) {
        case GEOS_POINT.rawValue:
            subclass = Waypoint.self

        case GEOS_LINESTRING.rawValue:
            subclass = LineString.self

        case GEOS_LINEARRING.rawValue:
            subclass = LinearRing.self

        case GEOS_POLYGON.rawValue:
            subclass = Polygon.self

        case GEOS_MULTIPOINT.rawValue:
            subclass = MultiPoint.self

        case GEOS_MULTILINESTRING.rawValue:
            subclass = MultiLineString.self

        case GEOS_MULTIPOLYGON.rawValue:
            subclass = MultiPolygon.self

        case GEOS_GEOMETRYCOLLECTION.rawValue:
            subclass = GeometryCollection<Geometry>.self

        default:
            return nil
        }
        return subclass
    }

    /**
     Create a Geometry subclass from its Well Known Text representation.

     - parameter WKT: The geometry representation in Well Known Text format (i.e. `POINT(10 45)`).

     - returns: The proper Geometry subclass as parsed from the string (i.e. `Waypoint`).
     */
    @objc open class func create(_ WKT: String) -> Geometry? {
        let WKTReader = GEOSWKTReader_create_r(GEOS_HANDLE)
        defer { GEOSWKTReader_destroy_r(GEOS_HANDLE, WKTReader) }
        guard let GEOSGeom = GEOSWKTReader_read_r(GEOS_HANDLE, WKTReader, (WKT as NSString).utf8String) else {
            return nil
        }
        return self.create(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
    }

    /**
     Create a Geometry subclass from its Well Known Binary representation.

     - parameter WKB: The geometry representation in Well Known Binary format.
     - parameter size: The size of the binary representation in bytes.

     - returns: The proper Geometry subclass as parsed from the binary data (i.e. `Waypoint`).
     */
    open class func create(_ WKB: UnsafePointer<UInt8>, size: Int) -> Geometry? {
        let WKBReader = GEOSWKBReader_create_r(GEOS_HANDLE)
        defer { GEOSWKBReader_destroy_r(GEOS_HANDLE, WKBReader) }
        guard let GEOSGeom = GEOSWKBReader_read_r(GEOS_HANDLE, WKBReader, WKB, size) else {
            return nil
        }
        return self.create(storage: GeometryStorage(GEOSGeom: GEOSGeom, parent: nil))
    }

    /// The Well Known Text (WKT) representation of the Geometry.
    @objc fileprivate(set) open lazy var WKT: String? = {
        let WKTWriter = GEOSWKTWriter_create_r(GEOS_HANDLE)
        GEOSWKTWriter_setTrim_r(GEOS_HANDLE, WKTWriter, 1)
        guard let wktString = GEOSWKTWriter_write_r(GEOS_HANDLE, WKTWriter, storage.GEOSGeom) else {
            return nil
        }
        let wkt = String(cString: wktString)
        free(wktString)
        GEOSWKTWriter_destroy_r(GEOS_HANDLE, WKTWriter)
        return wkt
    }()

    /// The Well Known Binary (WKB) representation of the Geometry.
    fileprivate(set) open lazy var WKB: [UInt8]? = {
        let WKBWriter = GEOSWKBWriter_create_r(GEOS_HANDLE)
        var size: Int = 0
        guard let buf = GEOSWKBWriter_write_r(GEOS_HANDLE, WKBWriter, storage.GEOSGeom, &size), size > 0 else {
            return nil
        }
        let wkb = Array(UnsafeBufferPointer(start: buf, count: size))
        free(buf)
        GEOSWKBWriter_destroy_r(GEOS_HANDLE, WKBWriter)
        return wkb
    }()

    /// Returns true if the two Geometries are exactly equal. This gives Geometry and its
    /// subclasses an Equatable behavior that is based on the geometry value rather than
    /// on object identity (the NSObject default). To compare object identity, use ===
    open override func isEqual(_ object: Any?) -> Bool {
        guard let other = object as? Geometry else {
            return false
        }

        return GEOSEquals_r(GEOS_HANDLE, storage.GEOSGeom, other.storage.GEOSGeom) > 0
    }
}

public struct CoordinatesCollection: Collection {
    private let storage: GeometryStorage
    public let count: UInt32

    init(storage: GeometryStorage) {
        self.storage = storage
        let sequence = GEOSGeom_getCoordSeq_r(GEOS_HANDLE, storage.GEOSGeom)
        var numCoordinates: UInt32 = 0
        GEOSCoordSeq_getSize_r(GEOS_HANDLE, sequence, &numCoordinates)
        self.count = numCoordinates
    }

    public var startIndex: UInt32 {
        return 0
    }

    public var endIndex: UInt32 {
        return count
    }

    public func index(after i: UInt32) -> UInt32 {
        return i + 1
    }

    public subscript(index: UInt32) -> Coordinate {
        var x: Double = 0
        var y: Double = 0

        assert(self.count > index, "Index out of bounds")
        let sequence = GEOSGeom_getCoordSeq_r(GEOS_HANDLE, storage.GEOSGeom)
        GEOSCoordSeq_getX_r(GEOS_HANDLE, sequence, index, &x)
        GEOSCoordSeq_getY_r(GEOS_HANDLE, sequence, index, &y)

        return Coordinate(x: x, y: y)
    }

    public func makeIterator() -> AnyIterator<Coordinate> {
        var index: UInt32 = 0
        return AnyIterator {
            guard index < self.count else { return nil }
            let item = self[index]
            index += 1
            return item
        }
    }

    public func map<U>(_ transform: (Coordinate) -> U) -> [U] {
        var array = [U]()
        for coord in self {
            array.append(transform(coord))
        }
        return array
    }
}

public struct GeometriesCollection<T: Geometry>: Sequence {
    private let storage: GeometryStorage
    public let count: Int32

    init(storage: GeometryStorage) {
        self.storage = storage
        self.count = GEOSGetNumGeometries_r(GEOS_HANDLE, storage.GEOSGeom)
    }

    public subscript(index: Int32) -> T {
        let GEOSGeom = GEOSGetGeometryN_r(GEOS_HANDLE, storage.GEOSGeom, index)!
        let childStorage = GeometryStorage(GEOSGeom: GEOSGeom, parent: storage)
        return Geometry.create(storage: childStorage) as! T
    }

    public func makeIterator() -> AnyIterator<T> {
        var index: Int32 = 0
        return AnyIterator {
            guard index < self.count else { return nil }
            let item = self[index]
            index += 1
            return item
        }
    }

    public func map<U>(_ transform: (T) -> U) -> [U] {
        var array = [U]()
        for geom in self {
            array.append(transform(geom))
        }
        return array
    }
}

public struct Coordinate: Hashable {
    public let x: CoordinateDegrees
    public let y: CoordinateDegrees

    public init(x: CoordinateDegrees, y: CoordinateDegrees) {
        self.x = x
        self.y = y
    }
}
