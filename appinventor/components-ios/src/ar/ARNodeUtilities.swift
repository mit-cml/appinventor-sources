import Foundation
import RealityFoundation
import os.log
import ARKit


@available(iOS 14.0, *)
class ARNodeUtilities {
    private static let ERROR_UNKNOWN_TYPE = "Unknown type in JSON conversion"
    

  public static func parseYailToNode(_ node: ARNodeBase, _ yailObj: Any, _ trackingObj: ARSession) -> ARNodeBase {
        do {
            guard let keyvalue = yailObj as? YailDictionary else {
                throw NSError(domain: "ParseError", code: 1, userInfo: [NSLocalizedDescriptionKey: "Invalid Yail object"])
            }
            
            let model = keyvalue["model"] as? String ?? ""
            let texture = keyvalue["texture"] as? String ?? ""
            let type = keyvalue["type"] as? String ?? ""
            let scale = keyvalue["scale"] as? String ?? ".2"
            print("parsed model \(model)")
            print("parsed texture \(texture)")
 
            
            node.ModelUrl = model // really only for free form models
            node.Texture = texture
            node.Scale = Float(scale) ?? 0.5

            if let poseDict = keyvalue["pose"] as? [String: Any] {
                print("parsed pose before conversion \( poseDict)")

                if let pose = parsePoseLinkedHashMap(poseDict) {
                  let transform = Transform(matrix: pose)
                  let anchor = node.createAnchorWithPose(pose: transform)
                  
                  if let geoAnchor = parseGeoData(poseDict){
                    node.setGeoAnchor(geoAnchor)
                    print("geoAnchor now \( geoAnchor)")
                  }

                  
                }
            }
          
       
            
        } catch {
          print("parseYailToNode error")
            //throw error
        }
        
        return node
    }
    
  public static func jsonObjectToYail(_ logTag: String, _ object: [String: Any]) throws -> YailList<AnyObject> {
      var pairs: [Any] = []
        
        for (key, value) in object {
            os_log("value is %@", log: .default, type: .info, String(describing: value))
            
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
      
      guard let geoData = poseDict["geoData"] as? [String: Double],
        
          let lat = geoData["lat"],
          let lng = geoData["lng"],
          let alt = geoData["alt"]
      else {
            return nil
        }
      // Create geo anchor
      let coordinate = CLLocationCoordinate2D(latitude: lat, longitude: lng)
      let geoAnchor = ARGeoAnchor(coordinate: coordinate,altitude: alt)
      return geoAnchor
    }
    // Helper method
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
        
        // Create transform matrix from translation and rotation
        let quaternion = simd_quatf(ix: qx, iy: qy, iz: qz, r: qw)
        let rotationMatrix = simd_float4x4(quaternion)
        
        var transform = rotationMatrix
        transform.columns.3 = SIMD4<Float>(x, y, z, 1.0)
        
        return transform
    }
}

