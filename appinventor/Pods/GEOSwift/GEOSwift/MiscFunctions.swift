//
//  MiscFunctions.swift
//
//  Created by Andrea Cremaschi on 03/02/16.
//  Copyright (c) 2016 andreacremaschi. All rights reserved.
//

import Foundation

public extension Geometry {
    /// - returns: The distance between the two geometries, expressed in the SRID of the first
    func distance(geometry: Geometry) -> Double {
        var dist: Double = 0
        let result = GEOSDistance_r(GEOS_HANDLE, storage.GEOSGeom, geometry.storage.GEOSGeom, &dist)
        assert(result == 1)
        return dist
    }
}
