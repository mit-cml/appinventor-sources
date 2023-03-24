// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright 2018-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

import Foundation

private let FILENAME_PREFIX: String = "app_inventor_"
private let DIRECTORY_RECORDINGS: String = "Recordings"
private let ANDROID_TO_IOS_RECORDING_EXTENSION: String = "aac"
private let DIRECTORY_PICTURES: String = "Pictures"

open class FileUtil {
  
  public static func getRecordingFile(_ fileExtension: String) throws -> String {
    return try getFile(DIRECTORY_RECORDINGS, fileExtension)
  }
  
  public static func getRecordingFileFromAndroidPath(_ filePath: String) throws -> String {
    let aacFilePath = transformFileExtension(filePath, toExtension: ANDROID_TO_IOS_RECORDING_EXTENSION)
    return try transformAndroidFilePath(aacFilePath)
  }
  
  public static func getPictureFile(_ fileExtension: String) throws -> String {
    return try getFile(DIRECTORY_PICTURES, fileExtension)
  }
  
  public static func transformFileExtension(_ fileName: String, toExtension fileExtension: String) -> String {
    let originalName: NSString = NSString(string: fileName)
    let pathPrefix: NSString = NSString(string: originalName.deletingPathExtension)
    let transformedName = pathPrefix.appendingPathExtension(fileExtension)
    return transformedName!
  }
  
  public static func transformAndroidFilePath(_ filePath: String) throws -> String {
    let filePath = AssetManager.shared.transformPotentialAndroidPath(path: filePath)
    
    try createFullFilePath(filePath, isAppend: false)
    return filePath
  }
  
  /**
   * Used to create absolute file names as specified by the File Component
   */
  public static func absoluteFileName(_ fileName: String, _ isRepl: Bool) -> String {
    var filePath = ""
    if fileName.starts(with: "//") {
      let postSlashIndex = fileName.index(fileName.startIndex, offsetBy: 2)
      if isRepl {
        let file = String(fileName[postSlashIndex...])
        filePath = AssetManager.shared.pathForExistingFileAsset(file)
      } else {
        filePath = AssetManager.shared.pathForAssetInBundle(filename: String(fileName[postSlashIndex...]))
      }
    } else if fileName.starts(with: "/") {
      let path = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
      filePath = "\(path)\(fileName)"
    } else {
      if isRepl {
        filePath = AssetManager.shared.pathForPublicAsset("data/\(fileName)")
      } else {
        let path = NSSearchPathForDirectoriesInDomains(.applicationSupportDirectory, .userDomainMask, true)[0]
        filePath = "\(path)/\(fileName)"
      }
    }
    return filePath
  }
  
  private static func getFile(_ directory: String, _ fileExtension: String) throws -> String {
    let currentTimeInMS = String(Int(NSDate().timeIntervalSince1970 * 1000))
    let relativePath = directory + "/" + FILENAME_PREFIX + currentTimeInMS + "." + fileExtension
    let filePath = AssetManager.shared.pathForPrivateAsset(relativePath)
    
    try createFullFilePath(filePath, isAppend: false)
    return filePath
  }
  
  public static func createFullFilePath(_ filePath: String, isAppend: Bool) throws {
    let files = FileManager()
    do {
      try files.createDirectory(atPath: NSString(string: filePath).deletingLastPathComponent, withIntermediateDirectories: true, attributes: nil)
      if !isAppend && files.fileExists(atPath: filePath) {
        try files.removeItem(atPath: filePath)
      }
    } catch {
      throw FileError(ErrorMessage.ERROR_CANNOT_WRITE_TO_FILE, filePath)
    }
  }
}
