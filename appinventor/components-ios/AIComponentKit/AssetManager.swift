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
  private let appname: String
  private let privatePath: String
  private let cachePath: String
  private let publicPath: String

  public enum AssetScope {
    case BUNDLE
    case PRIVATE
    case CACHE
    case PUBLIC
  }

  private override init() {
    appname = "AppInventor"
    var path = NSSearchPathForDirectoriesInDomains(.applicationSupportDirectory, .userDomainMask, true)[0]
    cachePath = "\(path)/AppInventor"
    path = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)[0]
    privatePath = "\(path)/AppInventor"
    path = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
    publicPath = "\(path)/AppInventor"
  }
  
  init(for unpackedApp: Application) {
    appname = unpackedApp.name
    privatePath = NSSearchPathForDirectoriesInDomains(.applicationSupportDirectory, .userDomainMask, true)[0]
    cachePath = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)[0]
    publicPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
  }
  
  public class var shared: AssetManager {
    get {
      if manager == nil {
        manager = AssetManager()
      }
      return manager!
    }
  }

  public func pathForAsset(filename: String, scope: AssetScope) -> String {
    switch scope {
      case .BUNDLE:
        return pathForAssetInBundle(filename: filename)
      case .PRIVATE:
        return pathForPrivateAsset(filename: filename)
      case .CACHE:
        return pathForCacheAsset(filename: filename)
      case .PUBLIC:
        return pathForPublicAsset(filename: filename)
    }
  }

  public func pathForPublicAsset(filename: String) -> String {
    let documentDir = publicPath
    return "\(documentDir)/\(filename)"
  }
  
  public func pathForCacheAsset(filename: String) -> String {
    let documentDir = cachePath
    return "\(documentDir)/\(filename)"
  }
  
  public func pathForPrivateAsset(filename: String) -> String {
    let documentDir = privatePath
    return "\(documentDir)/\(filename)"
  }

  public func pathForExistingFileAsset(filename: String) -> String {
    var path = pathForPrivateAsset(filename: filename)
    if FileManager.default.fileExists(atPath: path) {
      return path
    }
    path = pathForCacheAsset(filename: filename)
    if FileManager.default.fileExists(atPath: path) {
      return path
    }
    path = pathForPublicAsset(filename: filename)
    if FileManager.default.fileExists(atPath: path) {
      return path
    }
    return ""
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
      return pathForExistingFileAsset(filename: filename)
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
