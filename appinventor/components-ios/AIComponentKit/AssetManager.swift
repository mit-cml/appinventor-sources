//
//  AssetManager.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/25/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

public class AssetManager: NSObject {
  private static var manager: AssetManager?
  public static var isRepl = false
  
  private override init() {
  }
  
  init(for unpackedApp: Application) {
    
  }
  
  public class var shared: AssetManager {
    get {
      if manager == nil {
        manager = AssetManager()
      }
      return manager!
    }
  }
  
  public func pathForAsset(filename: String) -> String {
    let documentDir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
    return "\(documentDir)/\(filename)"
  }
  
  public func pathForAssetInBundle(filename: String) -> String {
    let documentDir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
    let lastDot = filename.range(of: ".", options: String.CompareOptions.backwards, range: nil, locale: nil)
    if lastDot == nil {
      return "\(documentDir)/\(filename)"
    }

    let lastSlash = filename.range(of: ".", options: String.CompareOptions.backwards, range: nil, locale: nil)
    if lastSlash != nil {
      return "\(documentDir)/\(filename)"
    }

    let firstPart = filename.substring(to: (lastDot?.lowerBound)!)
    let lastPart = filename.substring(from: filename.index(after: (lastDot?.upperBound)!))
    let path = Bundle.main.path(forResource: firstPart, ofType: lastPart, inDirectory: documentDir)
    if path == nil {
      return pathForAsset(filename: filename)
    } else {
      return path!
    }
  }

  public func imageFromPath(path: String) -> UIImage? {
    if let image = UIImage(named: path) {
      return image
    }
    return nil
  }
}
