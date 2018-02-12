//
//  AssetManager.swift
//  AIComponentKit
//
//  Created by Evan Patton on 10/25/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

import Foundation

open class AssetManager: NSObject {
  fileprivate static var manager: AssetManager?
  open static var isRepl = false
  fileprivate let appname: String
  fileprivate let privatePath: String
  fileprivate let cachePath: String
  fileprivate let publicPath: String

  public enum AssetScope {
    case bundle
    case `private`
    case cache
    case `public`
  }

  override init() {
    appname = "AppInventor"
    do {
      var path = NSSearchPathForDirectoriesInDomains(.applicationSupportDirectory, .userDomainMask, true)[0]
      privatePath = "\(path)/AppInventor"
      path = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)[0]
      cachePath = "\(path)/AppInventor"
      path = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
      publicPath = "\(path)/AppInventor"
      let files = FileManager()
      try files.createDirectory(atPath: privatePath, withIntermediateDirectories: true, attributes: nil)
      try files.createDirectory(atPath: cachePath, withIntermediateDirectories: true, attributes: nil)
      try files.createDirectory(atPath: publicPath, withIntermediateDirectories: true, attributes: nil)
    } catch {
      NSLog("Unable to create directories")  // Should never happen in practice given sandboxing
    }
  }
  
  init(for unpackedApp: Application) {
    appname = unpackedApp.name
    do {
      privatePath = NSSearchPathForDirectoriesInDomains(.applicationSupportDirectory, .userDomainMask, true)[0]
      cachePath = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true)[0]
      publicPath = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
      let files = FileManager()
      try files.createDirectory(atPath: privatePath, withIntermediateDirectories: true, attributes: nil)
      try files.createDirectory(atPath: cachePath, withIntermediateDirectories: true, attributes: nil)
      try files.createDirectory(atPath: publicPath, withIntermediateDirectories: true, attributes: nil)
    } catch {
      NSLog("Unable to create directories")  // Should never happen in practice given sandboxing
    }
  }
  
  open class var shared: AssetManager {
    get {
      if manager == nil {
        manager = AssetManager()
      }
      return manager!
    }
  }

  open func pathForAsset(_ filename: String, scope: AssetScope) -> String {
    switch scope {
      case .bundle:
        return pathForAssetInBundle(filename: filename)
      case .private:
        return pathForPrivateAsset(filename)
      case .cache:
        return pathForCacheAsset(filename)
      case .public:
        return pathForPublicAsset(filename)
    }
  }

  open func pathForPublicAsset(_ filename: String) -> String {
    let documentDir = publicPath
    return "\(documentDir)/\(filename)"
  }
  
  open func pathForCacheAsset(_ filename: String) -> String {
    let documentDir = cachePath
    return "\(documentDir)/\(filename)"
  }
  
  open func pathForPrivateAsset(_ filename: String) -> String {
    let documentDir = privatePath
    return "\(documentDir)/\(filename)"
  }

  open func pathForExistingFileAsset(_ filename: String) -> String {
    var path = pathForPrivateAsset(filename)
    if FileManager.default.fileExists(atPath: path) {
      return path
    }
    path = pathForCacheAsset(filename)
    if FileManager.default.fileExists(atPath: path) {
      return path
    }
    path = pathForPublicAsset(filename)
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
      return pathForExistingFileAsset(filename)
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
  
  public func transformPotentialAndroidPath(path: String) -> String {
    let relativePathComp = path.components(separatedBy: "sdcard/").last!
    let relativePath = relativePathComp[relativePathComp.startIndex] == "/" ? relativePathComp.chopPrefix() : relativePathComp
    let iOSPath = relativePathComp == path ? AssetManager.shared.pathForPrivateAsset(relativePath) : AssetManager.shared.pathForPublicAsset(relativePath)
    return iOSPath
  }
}
