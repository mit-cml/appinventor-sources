// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology, All rights reserved.
import Foundation

class MapFeatureContainerBase: ViewComponent {
  let ERROR_CODE_MALFORMED_URL: Int32 = -1
  let ERROR_CODE_IO_EXCEPTION: Int32 = -2
  let ERROR_CODE_MALFORMED_GEOJSON: Int32 = -3
  let ERROR_CODE_UNKNOWN_TYPE: Int32
    = -4
  
  let ERROR_MALFORMED_URL: String = "The URL is malformed"
  let ERROR_IO_EXCEPTION: String = "Unabled to download content from URL"
  let ERROR_MALFORMED_GEOJSON: String = "Malformed GeoJSON response.  Expected FeatureCollection as root element."
  let ERROR_UNKNOWN_TYPE: String = "Unrecognized/invalid type in JSON object"
  let ERROR_GEOJSON_PARSE_ERORR: String = "Unable to parse JSON from url."

  let GEOJSON_FEATURECOLLECTION: String = "FeatureCollection"
  let GEOJSON_GEOMETRYCOLLECTION: String = "GeometryCollection"
  let GEOJSON_GEOMETRY: String = "geometry"
  let GEOJSON_PROPERTIES: String = "properties"
  let GEOJSON_FEATURE: String = "Feature"

  open func FeatureClick(_ feature: MapFeature) {}
}
