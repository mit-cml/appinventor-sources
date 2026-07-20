// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation
import ImageIO
import MobileCoreServices
import UIKit

@objc open class AnimatedGif: NSObject {
  let images: [UIImage]
  let cgImages: [CGImage]
  let frameDurations: [Double]
  let keyTimes: [NSNumber]
  let duration: Double
  let loopCount: Int

  var firstFrame: UIImage? {
    return images.first
  }

  init?(data: Data) {
    guard let source = CGImageSourceCreateWithData(data as CFData, nil) else {
      return nil
    }
    guard let type = CGImageSourceGetType(source), UTTypeConformsTo(type, kUTTypeGIF) else {
      return nil
    }
    let count = CGImageSourceGetCount(source)
    guard count > 1 else {
      return nil
    }

    var images = [UIImage]()
    var cgImages = [CGImage]()
    var durations = [Double]()
    for i in 0..<count {
      guard let cgImage = CGImageSourceCreateImageAtIndex(source, i, nil) else {
        continue
      }
      cgImages.append(cgImage)
      images.append(UIImage(cgImage: cgImage))
      durations.append(AnimatedGif.frameDuration(source: source, index: i))
    }
    guard images.count > 1 else {
      return nil
    }

    self.images = images
    self.cgImages = cgImages
    self.frameDurations = durations
    self.duration = durations.reduce(0.0, +)
    self.keyTimes = AnimatedGif.makeKeyTimes(durations)
    self.loopCount = AnimatedGif.loopCount(source: source)
  }

  private static func frameDuration(source: CGImageSource, index: Int) -> Double {
    var duration = 0.1
    if let properties = CGImageSourceCopyPropertiesAtIndex(source, index, nil) as? [CFString: Any],
       let gifProperties = properties[kCGImagePropertyGIFDictionary] as? [CFString: Any] {
      if let unclampedDelay = gifProperties[kCGImagePropertyGIFUnclampedDelayTime] as? Double {
        duration = unclampedDelay
      } else if let delay = gifProperties[kCGImagePropertyGIFDelayTime] as? Double {
        duration = delay
      }
    }
    return duration < 0.02 ? 0.1 : duration
  }

  private static func loopCount(source: CGImageSource) -> Int {
    if let properties = CGImageSourceCopyProperties(source, nil) as? [CFString: Any],
       let gifProperties = properties[kCGImagePropertyGIFDictionary] as? [CFString: Any],
       let loopCount = gifProperties[kCGImagePropertyGIFLoopCount] as? Int {
      return loopCount
    }
    return 0
  }

  private static func makeKeyTimes(_ durations: [Double]) -> [NSNumber] {
    let totalDuration = durations.reduce(0.0, +)
    guard totalDuration > 0 else {
      return durations.indices.map { NSNumber(value: Double($0) / Double(durations.count)) }
    }
    var elapsed = 0.0
    var keyTimes = [NSNumber]()
    for duration in durations {
      keyTimes.append(NSNumber(value: elapsed / totalDuration))
      elapsed += duration
    }
    return keyTimes
  }
}

open class AssetManager: NSObject {
  fileprivate static var manager: AssetManager?
  @objc public static var isRepl = false
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
  
  @objc init(for unpackedApp: Application) {
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
  
  @objc open class var shared: AssetManager {
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

  @objc open func pathForPublicAsset(_ filename: String) -> String {
    let documentDir = publicPath
    return "\(documentDir)/\(filename)"
  }
  
  @objc open func pathForCacheAsset(_ filename: String) -> String {
    let documentDir = cachePath
    return "\(documentDir)/\(filename)"
  }
  
  @objc open func pathForPrivateAsset(_ filename: String) -> String {
    let documentDir = privatePath
    return "\(documentDir)/\(filename)"
  }

  @objc open func pathForExistingFileAsset(_ filename: String) -> String {
    if FileManager.default.fileExists(atPath: filename) {
      return filename
    }
    var path = pathForAssetInApp(filename) ?? ""
    if !path.isEmpty && FileManager.default.fileExists(atPath: path) {
      return path
    }
    path = pathForPrivateAsset(filename)
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
    if filename.hasPrefix("file://") {
      return filename.chopPrefix(count: 7).removingPercentEncoding ?? ""
    }
    return ""
  }

  @objc public func pathForAssetInApp(_ filename: String) -> String? {
    guard let path = Application.current?.assetPath else {
      return nil
    }
    let assetPath = "\(path)/\(filename)"
    if FileManager.default.fileExists(atPath: assetPath) {
      return assetPath
    }
    return nil
  }

  @objc public func pathForAssetInBundle(filename: String) -> String {
    let documentDir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
    let lastDot = filename.range(of: ".", options: String.CompareOptions.backwards, range: nil, locale: nil)
    if lastDot == nil {
      return "\(documentDir)/\(filename)"
    }

    let lastSlash = filename.range(of: ".", options: String.CompareOptions.backwards, range: nil, locale: nil)
    if lastSlash != nil {
      return "\(documentDir)/\(filename)"
    }

    let firstPart = String(filename[..<(lastDot?.lowerBound)!])
    let lastPart = String(filename[filename.index(after: (lastDot?.upperBound)!)...])
    let path = Bundle.main.path(forResource: firstPart, ofType: lastPart, inDirectory: documentDir)
    if path == nil {
      return pathForExistingFileAsset(filename)
    } else {
      return path!
    }
  }

  @objc public func imageFromPath(path: String) -> UIImage? {
    if path.isEmpty {
      return nil
    } else if let image =  UIImage(contentsOfFile: pathForExistingFileAsset(path)){
      return image
    } else if let image = UIImage(named: path) {
      return image
    } else if let image = UIImage(contentsOfFile: path) {
      return image
    } else if path.starts(with: "file://"), let image = UIImage(contentsOfFile: path.chopPrefix(count: 7).removingPercentEncoding ?? "") {
      return image
    }
    return nil
  }

  @objc public func animatedGifFromPath(path: String) -> AnimatedGif? {
    guard let data = dataFromPath(path: path) else {
      return nil
    }
    return AnimatedGif(data: data)
  }

  @objc public func dataFromPath(path: String) -> Data? {
    if path.isEmpty {
      return nil
    }
    let existingPath = pathForExistingFileAsset(path)
    if !existingPath.isEmpty, let data = try? Data(contentsOf: URL(fileURLWithPath: existingPath)) {
      return data
    } else if let assetPath = Bundle.main.path(forResource: path, ofType: nil),
              let data = try? Data(contentsOf: URL(fileURLWithPath: assetPath)) {
      return data
    } else if let data = try? Data(contentsOf: URL(fileURLWithPath: path)) {
      return data
    } else if path.starts(with: "file://"),
              let filePath = path.chopPrefix(count: 7).removingPercentEncoding,
              let data = try? Data(contentsOf: URL(fileURLWithPath: filePath)) {
      return data
    }
    return nil
  }
  
  @objc public func transformPotentialAndroidPath(path: String) -> String {
    let relativePathComp = path.components(separatedBy: "sdcard/").last!
    let relativePath = relativePathComp[relativePathComp.startIndex] == "/" ? relativePathComp.chopPrefix() : relativePathComp
    let iOSPath = relativePathComp == path ? AssetManager.shared.pathForPrivateAsset(relativePath) : AssetManager.shared.pathForPublicAsset(relativePath)
    return iOSPath
  }
}
