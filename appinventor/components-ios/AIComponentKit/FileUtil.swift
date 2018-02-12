// -*- mode: swift; swift-mode:basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

import Foundation

private let FILENAME_PREFIX: String = "app_inventor_"
private let DIRECTORY_RECORDINGS: String = "Recordings"
private let ANDROID_TO_IOS_RECORDING_EXTENSION: String = "aac"

open class FileUtil {
  
  open static func getRecordingFile(_ fileExtension: String) throws -> String {
    return try getFile(DIRECTORY_RECORDINGS, fileExtension)
  }
  
  open static func getRecordingFileFromAndroidPath(_ filePath: String) throws -> String {
    let aacFilePath = transformFileExtension(filePath, toExtension: ANDROID_TO_IOS_RECORDING_EXTENSION)
    return try transformAndroidFilePath(aacFilePath)
  }
  
  open static func transformFileExtension(_ fileName: String, toExtension fileExtension: String) -> String {
    let originalName: NSString = NSString(string: fileName)
    let pathPrefix: NSString = NSString(string: originalName.deletingPathExtension)
    let transformedName = pathPrefix.appendingPathExtension(fileExtension)
    return transformedName!
  }
  
  open static func transformAndroidFilePath(_ filePath: String) throws -> String {
    let filePath = AssetManager.shared.transformPotentialAndroidPath(path: filePath)
    
    try createFullFilePath(filePath)
    return filePath
  }
  
  private static func getFile(_ directory: String, _ fileExtension: String) throws -> String {
    let currentTimeInMS = String(Int(NSDate().timeIntervalSince1970 * 1000))
    let relativePath = directory + "/" + FILENAME_PREFIX + currentTimeInMS + "." + fileExtension
    let filePath = AssetManager.shared.pathForPrivateAsset(relativePath)
    
    try createFullFilePath(filePath)
    return filePath
  }
  
  private static func createFullFilePath(_ filePath: String) throws {
    let files = FileManager()
    do {
      try files.createDirectory(atPath: NSString(string: filePath).deletingLastPathComponent, withIntermediateDirectories: true, attributes: nil)
      if files.fileExists(atPath: filePath) {
        try files.removeItem(atPath: filePath)
      }
    } catch {
      throw FileError(ErrorMessage.ERROR_CANNOT_WRITE_TO_FILE, filePath)
    }
  }
}
