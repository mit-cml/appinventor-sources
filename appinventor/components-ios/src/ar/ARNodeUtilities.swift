import Foundation
import RealityFoundation
import os.log
import ARKit


@available(iOS 14.0, *)
class ARNodeUtilities {
    private static let ERROR_UNKNOWN_TYPE = "Unknown type in JSON conversion"
    

  public static func parseYailToNode(_ node: ARNodeBase, _ yailObj: Any, _ trackingObj: ARSession, sessionStartLocation: CLLocation?) -> ARNodeBase {
    guard let keyvalue = yailObj as? YailDictionary else { return node }
    
    let model = keyvalue["model"] as? String ?? ""
    let texture = keyvalue["texture"] as? String ?? ""
    let scale = keyvalue["scale"] as? Float ?? 0.5
    let physics = keyvalue["physics"] as? Bool ?? true
    let canMove = keyvalue["canMove"] as? Bool ?? true
    let canScale = keyvalue["canScale"] as? Bool ?? true
    
    node.ModelUrl = model
    node.Texture = texture
    node.Scale = scale
    node.EnablePhysics = physics == true
    node.PanToMove = canMove == true
    node.PinchToScale = canScale == true
  
  // this doesn't appear to be working
    print("node from yail: \(node.Name) \(physics) \(canMove) \(canScale)")
    print("node from yail: \(node.Name) \(node.EnablePhysics) \(node.PanToMove) \(node.PinchToScale)")
    
    
    if let poseDict = keyvalue["pose"] as? [String: Any],
      let tDict = poseDict["t"] as? [String: Float] {
        
      let savedX = tDict["x"] ?? 0
      let savedZ = tDict["z"] ?? 0
      let yOffset = tDict["y_offset"]
      
      // Calculate Y position
      let currentFloorLevel = Float(ARView3D.SHARED_GROUND_LEVEL)
      let bounds = node._modelEntity.visualBounds(relativeTo: nil)
      let halfHeight = (bounds.max.y - bounds.min.y) / 2
      
      let finalY: Float
      if let offset = yOffset {
          finalY = currentFloorLevel + offset
      } else {
          let savedY = tDict["y"] ?? 0
          finalY = max(savedY, currentFloorLevel + halfHeight + 0.05)
      }
      
      // Check if we should use geo-anchoring
      var useGeoAnchor = false
      if let lat = poseDict["lat"] as? Double,
         let lng = poseDict["lng"] as? Double,
         let alt = poseDict["alt"] as? Double,
         let sessionStart = sessionStartLocation {
          
          let savedLocation = CLLocation(latitude: lat, longitude: lng)
          let distance = sessionStart.distance(from: savedLocation)
          
          // Only use geo-anchor if FAR from session start
          useGeoAnchor = distance > 10.0  // 50+ meters = use GPS
          
          if useGeoAnchor {
              let geoAnchor = ARGeoAnchor(coordinate: savedLocation.coordinate, altitude: alt)
              node.setGeoAnchor(geoAnchor)
              print("🌍 Using geo-anchor for distant node (\(distance)m away)")
          } else {
              print("📍 Using world coordinates for nearby node (\(distance)m away)")
          }
      }
      
      // Set position (for non-geo or as initial position for geo)
      node.setPosition(x: savedX, y: finalY, z: savedZ)
      print("✅ Node positioned at: (\(savedX), \(finalY), \(savedZ))")
      
      // Apply rotation
      if let qDict = poseDict["q"] as? [String: Float],
         let qx = qDict["x"], let qy = qDict["y"],
         let qz = qDict["z"], let qw = qDict["w"] {
          node._modelEntity.transform.rotation = simd_quatf(ix: qx, iy: qy, iz: qz, r: qw)
      }
    }
    
    return node
  }
        
  public static func jsonObjectToYail(_ logTag: String, _ object: [String: Any]) throws -> YailList<AnyObject> {
    var pairs: [Any] = []
        
    for (key, value) in object {
      if value is Bool || value is Int || value is Int64 || value is Double || value is String {
          pairs.append([key, value])
      } else if let arrayValue = value as? [Any] {
          let yailArray = try jsonArrayToYail(logTag, arrayValue)
          pairs.append([key, yailArray])
      } else if let objectValue = value as? [String: Any] {
          let yailObject = try jsonObjectToYail(logTag, objectValue)
          pairs.append([key, yailObject])
      } else if value is NSNull {
          // Skip null values or handle as needed
          continue
      } else {
          os_log("%@: %@", log: .default, type: .fault, ERROR_UNKNOWN_TYPE, String(describing: type(of: value)))
          throw NSError(domain: "JSONConversionError", code: 1, userInfo: [NSLocalizedDescriptionKey: ERROR_UNKNOWN_TYPE])
      }
    }
      
    return pairs as! YailList<AnyObject>
  }
    
  public static func jsonArrayToYail(_ logTag: String, _ array: [Any]) throws -> YailList<AnyObject> {
    var items: [Any] = []
    
    for value in array {
      if value is Bool || value is Int || value is Int64 || value is Double || value is String {
          items.append(value)
      } else if let arrayValue = value as? [Any] {
          let yailArray = try jsonArrayToYail(logTag, arrayValue)
          items.append(yailArray)
      } else if let objectValue = value as? [String: Any] {
          let yailObject = try jsonObjectToYail(logTag, objectValue)
          items.append(yailObject)
      } else if value is NSNull {
          // Skip null values or handle as needed
          continue
      } else {
          os_log("%@: %@", log: .default, type: .fault, ERROR_UNKNOWN_TYPE, String(describing: type(of: value)))
          throw NSError(domain: "JSONConversionError", code: 1, userInfo: [NSLocalizedDescriptionKey: ERROR_UNKNOWN_TYPE])
      }
    }
    
    return items as! YailList<AnyObject>
  }
  
  private static func parseGeoData(_ poseDict: [String: Any]) -> ARGeoAnchor? {
    let lat = poseDict["lat"]
    let lng = poseDict["lng"]
    let alt = poseDict["alt"]

    let coordinate = CLLocationCoordinate2D(latitude: lat as! CLLocationDegrees, longitude: lng as! CLLocationDegrees)
    let geoAnchor = ARGeoAnchor(coordinate: coordinate,altitude: (alt as! CLLocationDistance))
    return geoAnchor
  }
  
  private static func parsePoseLinkedHashMap(_ poseDict: [String: Any]) -> simd_float4x4? {

    guard let translation = poseDict["t"] as? [String: Float],
          let rotation = poseDict["q"] as? [String: Float],
          let x = translation["x"],
          let y = translation["y"],
          let z = translation["z"],
          let qx = rotation["x"],
          let qy = rotation["y"],
          let qz = rotation["z"],
          let qw = rotation["w"]
    else {
        return nil
    }
  
    let yOffset = translation["y_offset"]
    
    // Create transform matrix from translation and rotation
    let quaternion = simd_quatf(ix: qx, iy: qy, iz: qz, r: qw)
    let rotationMatrix = simd_float4x4(quaternion)
    
    var transform = rotationMatrix
    transform.columns.3 = SIMD4<Float>(x, y, z, 1.0)
    
    return transform
  }
}

